package info.voidev.lspidea.dummy

import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import info.voidev.lspidea.misc.LspFakeLanguage

object LspElementTypes {
    object File : IFileElementType("LspElementTypes.File", LspFakeLanguage)
    object Content : IElementType("LspElementTypes.Content", LspFakeLanguage)
}
