package info.voidev.lspidea.lspex.inlay

import org.eclipse.lsp4j.Command
import org.eclipse.lsp4j.LocationLink

open class InlayHintLabelPart {

    lateinit var value: String

    var tooltip: String? = null

    var location: LocationLink? = null

    var command: Command? = null

}
