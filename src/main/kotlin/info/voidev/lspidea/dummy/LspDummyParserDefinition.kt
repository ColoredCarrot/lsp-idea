package info.voidev.lspidea.dummy

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.TokenSet

class LspDummyParserDefinition : ParserDefinition {

    override fun createLexer(project: Project) = LspDummyLexer()

    override fun createParser(project: Project) = LspDummyParser

    override fun getFileNodeType() = LspElementTypes.File

    override fun getCommentTokens(): TokenSet = TokenSet.EMPTY

    override fun getStringLiteralElements(): TokenSet = TokenSet.EMPTY

    override fun createElement(node: ASTNode): PsiElement {
        if (node.elementType == LspElementTypes.Content) {
            return LspDummyPsiElement(node)
        }
        error("Cannot createElement for alien node element type ${node.elementType}")
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return LspDummyPsiFile(viewProvider)
    }

}
