package info.voidev.lspidea.validation

import com.intellij.openapi.progress.ProgressIndicator
import info.voidev.lspidea.connect.LspServerProcessHandler
import info.voidev.lspidea.def.ConfiguredLspServer

interface LspServerValidator {

    fun validate(server: ConfiguredLspServer, runner: LspServerProcessHandler, progress: ProgressIndicator): LspServerValidationResult

    companion object : LspServerValidator by LspServerValidatorBase()
}
