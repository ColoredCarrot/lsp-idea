package info.voidev.lspidea.connect

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.util.UserDataHolderEx
import info.voidev.lspidea.LspSessionManager
import info.voidev.lspidea.event.LspSessionListener
import info.voidev.lspidea.features.codeaction.LspCodeActionManager
import info.voidev.lspidea.features.highlight.LspSemanticTokensManager
import info.voidev.lspidea.files.LspOpenDocumentsManager
import info.voidev.lspidea.lspstream.LspStreamingSupport
import info.voidev.lspidea.progress.LspProgressManager
import java.time.Instant

/**
 * A wrapper around session state which only exists for sessions whose state is at least [LspStatus.ACTIVE].
 *
 * Also provides some convenience behavioural methods.
 *
 * Child disposables registered with [Disposer.register]
 * are disposed just before the session begins finalization.
 */
class LspSession(val state: LspSessionState, val project: Project) : Disposable, UserDataHolderEx by UserDataHolderBase() {

    val createdWhen = Instant.now()!!

    val server get() = state.server
    val isActive get() = state.status == LspStatus.ACTIVE

    val openDocumentsManager = LspOpenDocumentsManager(this)
    val progressManager = LspProgressManager(this)
    val codeActionManager = LspCodeActionManager(this)
    val semanticTokensManager = LspSemanticTokensManager(this)
    val streamingSupport = LspStreamingSupport(this)

    init {
        project.messageBus.syncPublisher(LspSessionListener.TOPIC).newSessionAtomic(this)
    }

    /**
     * Shortcut for [Disposer.dispose]`(this)`.
     */
    fun destroy() {
        Disposer.dispose(this)
    }

    @Synchronized
    override fun dispose() {
        if (state.status == LspStatus.STOPPED) return

        LspSessionManager.getInstance(project).unregisterSession(this)
        LspSessionDestructor.destruct(state)
    }
}
