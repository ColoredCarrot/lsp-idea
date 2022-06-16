package info.voidev.lspidea.features.paraminfo

import com.intellij.lang.parameterInfo.CreateParameterInfoContext
import com.intellij.lang.parameterInfo.ParameterInfoHandler
import com.intellij.lang.parameterInfo.ParameterInfoUIContext
import com.intellij.lang.parameterInfo.UpdateParameterInfoContext
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.util.containers.map2Array
import info.voidev.lspidea.LspSessionManager
import info.voidev.lspidea.dummy.LspDummyPsiFile
import info.voidev.lspidea.util.identifyForLsp
import info.voidev.lspidea.util.joinUnwrapExceptionsCancellable
import info.voidev.lspidea.util.offset2lspPosition
import org.eclipse.lsp4j.ParameterInformation
import org.eclipse.lsp4j.SignatureHelp
import org.eclipse.lsp4j.SignatureHelpContext
import org.eclipse.lsp4j.SignatureHelpParams
import org.eclipse.lsp4j.SignatureHelpTriggerKind
import org.eclipse.lsp4j.SignatureInformation

class LspParameterInfoHandler : ParameterInfoHandler<PsiElement, Any> {

    /*
    Note:
    We don't use LSP's triggerCharacters,
    because we refresh always anyway.
     */

    override fun findElementForParameterInfo(context: CreateParameterInfoContext): PsiElement? {
        val psiFile = context.file as? LspDummyPsiFile ?: return null
        val psiContent = psiFile.contentDummyPsi ?: return null
        val file = psiFile.virtualFile ?: return null
        val session = LspSessionManager.getInstance(context.project).getForFile(file) ?: return null
        val document = context.editor.document
        val pos = document.offset2lspPosition(context.offset)

        val requestCtx = SignatureHelpContext().apply {
            triggerKind = SignatureHelpTriggerKind.Invoked
            activeSignatureHelp = null
            setIsRetrigger(false)
        }
        val resp = session.server.textDocumentService
            .signatureHelp(SignatureHelpParams(file.identifyForLsp(), pos, requestCtx))
            .joinUnwrapExceptionsCancellable() ?: return null

        context.itemsToShow = generateOverloads(resp) ?: return null

        return psiContent
    }

    private fun generateOverloads(resp: SignatureHelp): Array<Overload>? {
        val overloads: List<SignatureInformation> = resp.signatures
        if (overloads.isEmpty()) {
            return null // TODO: Is there a better handling strategy for this?
        }

        // Ensure internal consistency (only bad servers don't follow that)
        if (!isCoherent(resp)) {
            thisLogger().debug("Language server sent internally inconsistent signature information: $resp")
            return null
        }

        val activeOverloadIdx = coerceIndex(resp.activeSignature, overloads)
        val activeOverload = overloads[activeOverloadIdx]

        val activeParamIdx =
            coerceIndex(activeOverload.activeParameter ?: resp.activeParameter, activeOverload.parameters)

        return overloads.map2Array { overloadInfo ->
            val isActiveOverload = overloadInfo === activeOverload
            Overload(overloadInfo, if (isActiveOverload) activeParamIdx else -1, isActiveOverload, resp)
        }
    }

    private fun isCoherent(signatureHelp: SignatureHelp): Boolean {
        return signatureHelp.signatures.all { isParamListCoherent(it.parameters) }
    }

    private fun isParamListCoherent(params: List<ParameterInformation>?): Boolean {
        if (params == null || params.isEmpty()) return true
        val labelIsLeft = params[0].label.isLeft
        return params.all { it.label.isLeft == labelIsLeft }
    }

    private fun coerceIndex(i: Int?, list: List<Any>?): Int {
        return if (list.isNullOrEmpty()) -1 else (i ?: 0).coerceIn(list.indices)
    }

    override fun showParameterInfo(element: PsiElement, context: CreateParameterInfoContext) {
        context.showHint(element, context.offset, this)
    }

    override fun findElementForUpdatingParameterInfo(context: UpdateParameterInfoContext): PsiElement? {
        context.isPreservedOnHintHidden = false
        return if (onCaretMoved(context)) {
            context.parameterOwner
        } else {
            context.removeHint()
            null
        }
    }

    /**
     * Sends a new request to the language server.
     * @return `true` if the signature help is still valid and should continue to be displayed.
     */
    private fun onCaretMoved(context: UpdateParameterInfoContext): Boolean {
        val file = context.file.virtualFile ?: return false
        val session = LspSessionManager.getInstance(context.project).getForFile(file) ?: return false
        val document = context.editor.document
        val pos = document.offset2lspPosition(context.offset)

        @Suppress("UNCHECKED_CAST")
        val currentOverloads = context.objectsToView as Array<Overload>
        assert(currentOverloads.isNotEmpty())

        val requestCtx = SignatureHelpContext().apply {
            triggerKind = SignatureHelpTriggerKind.ContentChange
            activeSignatureHelp = currentOverloads.first().originalLspResponse
            setIsRetrigger(true)
        }
        val resp = session.server.textDocumentService
            .signatureHelp(SignatureHelpParams(file.identifyForLsp(), pos, requestCtx))
            .joinUnwrapExceptionsCancellable() ?: return false

        val updatedOverloads = generateOverloads(resp) ?: return false

        // It's non-trivial to merge the current and updated overloads;
        // there's probably a better algorithm
        if (updatedOverloads.size != currentOverloads.size) {
            return false
        }
        for (i in currentOverloads.indices) {
            currentOverloads[i] = updatedOverloads[i]
        }
        return true
    }

    override fun updateParameterInfo(parameterOwner: PsiElement, context: UpdateParameterInfoContext) {
        context.setCurrentParameter((context.objectsToView.firstOrNull() as Overload?)?.activeParamIdx ?: -1)
        context.objectsToView.forEachIndexed { i, overload ->
            overload as Overload
            context.setUIComponentEnabled(i, overload.isActiveOverload)
        }
    }

    override fun updateUI(overload: Any?, context: ParameterInfoUIContext) {
        overload as Overload

        val text: String = overload.info.label
        val params: List<ParameterInformation>? = overload.info.parameters

        val highlightRange = getHighlightRange(text, params, overload.activeParamIdx)

        // If we ever want to generate the text ourselves based solely off the parameter list
        // in order to match IntelliJ's style more closely,
        // we have this:  CodeInsightBundle.message("parameter.info.no.parameters")

        context.setupUIComponentPresentation(
            text,
            highlightRange.startOffset,
            highlightRange.endOffset,
            !context.isUIComponentEnabled,
            false,
            false,
            context.defaultParameterColor
        )
    }

    private fun getHighlightRange(text: String, params: List<ParameterInformation>?, activeParamIdx: Int): TextRange {
        if (params.isNullOrEmpty() || activeParamIdx !in params.indices) {
            return TextRange.EMPTY_RANGE
        }

        val activeParamLabel = params[activeParamIdx].label
        if (activeParamLabel.isRight) {
            return TextRange(activeParamLabel.right.first, activeParamLabel.right.second)
        }

        // Param's label is provided not as a range of the signature text,
        // but as an immediate substring.

        // Compute offset for active parameter
        // (since it's not guaranteed that all parameters have distinct labels)
        val searchOff = params.asSequence()
            .take(activeParamIdx)
            .fold(0) { off, p -> text.indexOf(p.label.left, off) + p.label.left.length }

        val off = text.indexOf(activeParamLabel.left, searchOff)
        if (off < 0) {
            // Not actually a substring, bad server! Just don't support activeParameter then
            return TextRange.EMPTY_RANGE
        }

        return TextRange(off, off + activeParamLabel.left.length)
    }

    override fun supportsOverloadSwitching(): Boolean {
        return false // I guess?
    }

    private class Overload(
        val info: SignatureInformation,
        val activeParamIdx: Int,
        val isActiveOverload: Boolean,
        val originalLspResponse: SignatureHelp,
    )
}
