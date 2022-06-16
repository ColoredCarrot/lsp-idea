package info.voidev.lspidea.features.folding

import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.featurelist.LspFeature
import org.eclipse.lsp4j.FoldingRangeProviderOptions

object LspFoldingFeature : LspFeature {

    override val displayName get() = "Code Folding"

    override fun isAvailable(session: LspSession): Boolean {
        val serverFoldingCapabilities = session.state.serverCapabilities.foldingRangeProvider
            ?: return false

        if (serverFoldingCapabilities.isLeft) {
            return serverFoldingCapabilities.left
        }

        val options: FoldingRangeProviderOptions = serverFoldingCapabilities.right
        return true // TODO: Anything more to do here?
    }
}
