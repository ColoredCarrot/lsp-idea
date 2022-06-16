package info.voidev.lspidea.features.documentation

import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.light.LightElement
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.misc.LspFakeLanguage
import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.Position
import javax.swing.Icon

class LspFakePsiElementForDocumentation(
    val session: LspSession,
    val file: VirtualFile,
    val offsetInFile: Int,
    val positionInFile: Position,
    val info: Hover,
    manager: PsiManager,
) : LightElement(manager, LspFakeLanguage), ItemPresentation {
    override fun toString(): String = javaClass.simpleName

    override fun getPresentation() = this

    // TODO: Can we find a better solution? Perhaps send a signatureInfo request?
    override fun getPresentableText() = "Documented Item"

    override fun getIcon(unused: Boolean): Icon? = null
}
