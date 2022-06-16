package info.voidev.lspidea.features.formatting

import com.intellij.application.options.CodeStyle
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import info.voidev.lspidea.misc.LspFileType
import org.eclipse.lsp4j.FormattingOptions

object LspFormattingOptionsProvider {

    fun get(psi: PsiFile): FormattingOptions {
        // TODO Thoroughly test this
        return CachedValuesManager.getCachedValue(psi) {
            val codeStyleSettings = CodeStyle.getSettings(psi)
            val formattingOptions = make(codeStyleSettings)
            CachedValueProvider.Result.create(formattingOptions, codeStyleSettings.modificationTracker)
        }
    }

    private fun make(settings: CodeStyleSettings) = FormattingOptions().apply {
        //TODO
        tabSize = settings.getTabSize(LspFileType)
        isInsertSpaces = true
        isInsertFinalNewline = true
        isTrimFinalNewlines = true
        isTrimTrailingWhitespace = true
    }

}
