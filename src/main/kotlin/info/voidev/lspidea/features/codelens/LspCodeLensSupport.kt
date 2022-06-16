package info.voidev.lspidea.features.codelens

import org.eclipse.lsp4j.CodeLens
import javax.swing.Icon

/**
 * Support for custom parsing of code lenses.
 */
interface LspCodeLensSupport {

    // TODO move the logic that gets the Icon for the code lens here

    fun parseCodeLens(codeLens: CodeLens): Icon?
}
