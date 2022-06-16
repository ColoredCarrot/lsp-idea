package info.voidev.lspidea.plugins.bundled.rustanalyzer

import info.voidev.lspidea.command.clientside.LspSingleArgCommandExecutor
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.plugins.bundled.rustanalyzer.dto.RustAnalyzerRunnable
import info.voidev.lspidea.plugins.bundled.rustanalyzer.dto.RustAnalyzerRunnableKind
import org.eclipse.lsp4j.Command

class RustAnalyzerCommandExecutor : LspSingleArgCommandExecutor<RustAnalyzerRunnable>(
    "rust-analyzer.runSingle",
    argType = RustAnalyzerRunnable::class.java
) {

    override fun execute(arg: RustAnalyzerRunnable, command: Command, session: LspSession) {
        require(arg.kind == RustAnalyzerRunnableKind.Cargo)

        val runner = RustAnalyzerRunner(session)
        runner.run(arg.args, arg.label.ifBlank { "cargo" })
    }
}
