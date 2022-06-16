@file:Suppress("UnstableApiUsage")

package info.voidev.lspidea.features.inlay

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.intellij.codeInsight.hints.ChangeListener
import com.intellij.codeInsight.hints.FactoryInlayHintsCollector
import com.intellij.codeInsight.hints.ImmediateConfigurable
import com.intellij.codeInsight.hints.InlayHintsCollector
import com.intellij.codeInsight.hints.InlayHintsProvider
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.NoSettings
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import info.voidev.lspidea.LspSessionManager
import info.voidev.lspidea.dummy.LspDummyPsiElement
import info.voidev.lspidea.dummy.LspDummyPsiFile
import info.voidev.lspidea.lspex.inlay.InlayHint
import info.voidev.lspidea.lspex.inlay.InlayHintsParams
import info.voidev.lspidea.util.identifyForLsp
import info.voidev.lspidea.util.joinLsp
import info.voidev.lspidea.util.lspPosition2offset
import info.voidev.lspidea.util.range2lspRange
import org.eclipse.lsp4j.ServerCapabilities
import javax.swing.JComponent
import javax.swing.JPanel

class LspInlayProvider : InlayHintsProvider<NoSettings> {

    override val name get() = "LSP inlay"

    override val key get() = settingsKey

    override val previewText: String? get() = null

    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: NoSettings,
        sink: InlayHintsSink,
    ): InlayHintsCollector? {
        if (file !is LspDummyPsiFile) return null
        return object : FactoryInlayHintsCollector(editor) {
            override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
                if (element !is LspDummyPsiElement) return true

                val vfile = file.virtualFile ?: return false
                val session = LspSessionManager.getInstance(element.project).getForFile(vfile)
                    ?: return false

                if (!areInlayHintsSupported(session.state.serverCapabilities)) {
                    return false
                }

                val inlays = session.server.experimentalService.inlayHints(
                    InlayHintsParams(
                        vfile.identifyForLsp(),
                        editor.document.range2lspRange(0, editor.document.textLength)
                    )
                )
                    .joinLsp(session.project, "Could not fetch inlay hints")
                    .orEmpty()

                for (inlay in inlays) {
                    sink.addInlineElement(
                        editor.document.lspPosition2offset(inlay.position),
                        true,
                        factory.presentationForInlay(inlay),
                        false
                    )
                }

                return false
            }
        }
    }

    private fun areInlayHintsSupported(caps: ServerCapabilities): Boolean {
        val expCaps = caps.experimental as? JsonObject ?: return false
        val inlayHintsCap = expCaps.get("inlayHints") as? JsonPrimitive ?: return false

        return inlayHintsCap.asBoolean
    }

    private fun PresentationFactory.presentationForInlay(inlay: InlayHint): InlayPresentation {
        var presentation = text( // TODO or smallText
            buildString {
                if (inlay.paddingLeft == true) append(' ')
                append(inlay.label.left)
                if (inlay.paddingRight == true) append(' ')
            }
        )

        inlay.tooltip?.also {
            presentation = withTooltip(it, presentation)
        }

        return roundWithBackground(presentation)
    }

    override fun createConfigurable(settings: NoSettings) = object : ImmediateConfigurable {
        override fun createComponent(listener: ChangeListener): JComponent = JPanel()
    }

    override fun createSettings() = NoSettings()

    companion object {
        private val settingsKey = SettingsKey<NoSettings>("LspInlayProviderSettingsKey")
    }
}
