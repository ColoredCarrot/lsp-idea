package info.voidev.lspidea.featurelist

import info.voidev.lspidea.connect.LspSession
import org.jetbrains.annotations.Nls

interface LspFeature {

    val displayName: @Nls String

    fun isAvailable(session: LspSession): Boolean

}
