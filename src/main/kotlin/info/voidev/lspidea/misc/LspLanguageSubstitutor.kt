package info.voidev.lspidea.misc

import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.LanguageSubstitutor
import info.voidev.lspidea.def.LspServerDispatch

class LspLanguageSubstitutor : LanguageSubstitutor() {
    override fun getLanguage(file: VirtualFile, project: Project): Language? {
        LspServerDispatch.getInstance(project).getForFile(file) ?: return null
        return LspFakeLanguage
    }
}
