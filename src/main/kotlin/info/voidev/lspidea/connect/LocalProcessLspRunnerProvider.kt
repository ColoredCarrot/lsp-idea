package info.voidev.lspidea.connect

import info.voidev.lspidea.config.serverhandler.LocalProcessLspRunnerConfigState
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolute

class LocalProcessLspRunnerProvider(private val config: LocalProcessLspRunnerConfigState) : LspRunnerProvider {

    override fun getRunner(baseDir: Path): LspLocalServerProcessHandler {
        val processBuilder = ProcessBuilder(File(config.executablePath).absolutePath)
            .directory(baseDir.absolute().toFile())
        return LspLocalServerProcessHandler(processBuilder)
    }

}
