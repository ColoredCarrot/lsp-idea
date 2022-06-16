package info.voidev.lspidea.event

import com.intellij.util.messages.Topic
import com.intellij.util.messages.Topic.ProjectLevel
import info.voidev.lspidea.connect.LspSession

interface LspSessionListener {

    /**
     * There is no equivalent for the opposite event (session finalization);
     * instead, use the session as a parent [com.intellij.openapi.Disposable].
     *
     * Called atomically during session creation,
     * after the session has been initialized.
     * Consequently,
     * [info.voidev.lspidea.LspSessionManager.getForFile] or similar **must not**
     * be called from inside this method.
     */
    fun newSessionAtomic(session: LspSession) = Unit

    /**
     * Called in a background thread sometime after session initialization.
     */
    fun newSession(session: LspSession) = Unit

    companion object {
        @ProjectLevel
        @JvmStatic
        val TOPIC = Topic.create("LSP session notifications", LspSessionListener::class.java)
    }
}
