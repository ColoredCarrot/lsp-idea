package info.voidev.lspidea.download

import java.nio.file.Path
import java.util.concurrent.CompletionStage
import javax.swing.JComponent

interface LspServerExecutableInstaller {

    fun download(uiContext: JComponent): CompletionStage<Path>?

}
