package info.voidev.lspidea.features.structureview

import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

class LspStructureViewModel(psiFile: PsiFile, editor: Editor?, root: StructureViewTreeElement) : StructureViewModelBase(psiFile, editor, root)
