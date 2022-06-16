package info.voidev.lspidea.lspex.inlay

import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.jsonrpc.messages.Either

open class InlayHint {

    lateinit var label: Either<String, List<InlayHintLabelPart>>

    lateinit var position: Position

    var kind: InlayHintKind? = null

    var tooltip: String? = null

    var paddingLeft: Boolean? = null

    var paddingRight: Boolean? = null
}
