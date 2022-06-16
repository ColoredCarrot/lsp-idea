package info.voidev.lspidea.features.refactor

import com.intellij.lang.refactoring.RefactoringSupportProvider

class LspRefactoringSupportProvider : RefactoringSupportProvider() {

    override fun getExtractMethodHandler() = LspExtractMethodHandler

    override fun getExtractModuleHandler() = LspExtractModuleHandler

    override fun getIntroduceVariableHandler() = LspIntroduceVariableHandler

    override fun getIntroduceConstantHandler() = LspIntroduceConstantHandler
}
