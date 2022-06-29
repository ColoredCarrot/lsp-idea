package info.voidev.lspidea.connect

import info.voidev.lspidea.config.runner.LocalProcessLspRunnerConfigState
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolute

class LocalProcessLspRunnerProvider(private val config: LocalProcessLspRunnerConfigState) : LspRunnerProvider {
    override fun getRunner(baseDir: Path): LspLocalProcessRunner {
        val processBuilder = ProcessBuilder(File(config.executablePath).absolutePath)
            .directory(baseDir.absolute().toFile())
        return LspLocalProcessRunner(processBuilder)
    }
}
