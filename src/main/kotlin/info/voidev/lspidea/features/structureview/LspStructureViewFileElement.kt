package info.voidev.lspidea.features.structureview

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.Navigatable
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.misc.LspFileType

/**
 * Only used for files in which there are zero or more than one symbols
 */
class LspStructureViewFileElement(
    private val session: LspSession,
    private val file: VirtualFile,
    private val children: Array<StructureViewTreeElement>,
) : StructureViewTreeElement, ItemPresentation, Navigatable by OpenFileDescriptor(session.project, file, 0) {

    override fun getPresentableText() = file.presentableName

    override fun getIcon(unused: Boolean) = LspFileType.icon

    override fun getPresentation() = this

    override fun getChildren() = children

    override fun getValue() = file

}
