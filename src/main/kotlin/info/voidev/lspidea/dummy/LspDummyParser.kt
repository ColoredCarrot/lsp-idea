package info.voidev.lspidea.dummy

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType

object LspDummyParser : PsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val mRoot = builder.mark()
        val mContent = builder.mark()
        builder.advanceLexer()
        mContent.done(LspElementTypes.Content)
        mRoot.done(root)
        return builder.treeBuilt
    }
}
