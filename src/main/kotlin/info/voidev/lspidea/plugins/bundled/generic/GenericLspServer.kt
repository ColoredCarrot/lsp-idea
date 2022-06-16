package info.voidev.lspidea.plugins.bundled.generic

import info.voidev.lspidea.def.LspServer

class GenericLspServer : LspServer {

    override val displayName get() = "other"

    override val language get() = "generic"

}
