package info.voidev.lspidea.features.codelens

import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.featurelist.LspFeature

object CodeLensLspFeature : LspFeature {

    override val displayName get() = "Code Lens"

    override fun isAvailable(session: LspSession): Boolean {
        return session.state.serverCapabilitiesOrNull?.codeLensProvider != null
    }
}
