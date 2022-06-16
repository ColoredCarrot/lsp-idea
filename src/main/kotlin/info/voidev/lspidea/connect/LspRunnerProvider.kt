package info.voidev.lspidea.connect

import java.nio.file.Path

interface LspRunnerProvider {

    fun getRunner(baseDir: Path): LspServerProcessHandler
}
