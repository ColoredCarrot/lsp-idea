package info.voidev.lspidea.misc

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.event.LspSessionListener

class RehighlightFilesLspSessionListener : LspSessionListener {
    override fun newSession(session: LspSession) {
        DaemonCodeAnalyzer.getInstance(session.project).restart()
    }
}
