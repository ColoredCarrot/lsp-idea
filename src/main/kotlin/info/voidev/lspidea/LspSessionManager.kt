package info.voidev.lspidea

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.components.serviceIfCreated
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.vcs.log.submitSafe
import info.voidev.lspidea.config.InstantiableLspServer
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.connect.LspSessionConstructor
import info.voidev.lspidea.connect.LspSessionState
import info.voidev.lspidea.connect.LspStatus
import info.voidev.lspidea.def.ConfiguredLspServer
import info.voidev.lspidea.def.LspServerDispatch
import info.voidev.lspidea.event.LspSessionListener
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class LspSessionManager(private val project: Project) : Disposable {

    // accessed concurrently
    private val serverDispatch = LspServerDispatch.getInstance(project)
    // guarded by lock
    private val sessions = Reference2ObjectOpenHashMap<ConfiguredLspServer, LspSession>()

    private var autoCreateSessions = true

    private val lock = ReentrantReadWriteLock()

    internal fun unregisterSession(session: LspSession) = lock.write {
        if (sessions.remove(session.state.serverDef) == null) {
            throw IllegalArgumentException("No session for ${session.state.serverDef.displayName} is registered")
        }
    }

    fun getForFile(file: VirtualFile?): LspSession? {
        if (file == null) return null

        val serverDef = serverDispatch.getForFile(file) ?: return null
        val existingSession = lock.read { sessions[serverDef.definition] }
        if (existingSession != null) {
            if (existingSession.isActive) {
                return existingSession
            }
            thisLogger().error("existingSession with status ${existingSession.state.status}")
            return null
        }

        return if (autoCreateSessions) {
            createAndRegister(serverDef)
        } else {
            null
        }
    }

    fun getAll(): Collection<LspSession> = lock.read { sessions.values.toList() }

    fun getForIfActive(file: VirtualFile?): LspSession? {
        if (file == null) return null

        val serverDef = serverDispatch.getForFile(file) ?: return null

        return lock.read {
            sessions[serverDef.definition]?.takeIf { it.isActive }
        }
    }

    fun getFor(document: Document?): LspSession? {
        if (document == null) return null
        return getForFile(FileDocumentManager.getInstance().getFile(document))
    }

    private fun createAndRegister(server: InstantiableLspServer): LspSession? {
        val newState = LspSessionState(project, server.definition, server.runnerProvider.getRunner(project.baseDir.toNioPath()))
        val success = LspSessionConstructor.constructDefault(newState)

        if (!success) return null
        logger.assertTrue(newState.status == LspStatus.ACTIVE)

        // Cannot cancel because we need to run all our hooks to completion
        // before we invoke the session destruction listeners
        return ProgressManager.getInstance().computeInNonCancelableSection<LspSession, Exception> {
            lock.write {
                val existing = sessions[server.definition]
                if (existing != null) return@write existing

                val session = LspSession(newState, project)
                sessions[server.definition] = session
                newState.client.sessionOrNull = session

                session.state.process.onCrash().thenAccept { session.destroy() }

                AppExecutorUtil.getAppExecutorService().submitSafe(logger) {
                    if (session.isActive) {
                        project.messageBus.syncPublisher(LspSessionListener.TOPIC).newSession(session)
                    }
                }

                session
            }
        }
    }

    fun destroyAll() {
        while (true) {
            val toDestroy = lock.read { sessions.values.firstOrNull() }
                ?: break
            toDestroy.destroy()
        }
    }

    override fun dispose() {
        destroyAll()
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project) = project.service<LspSessionManager>()

        @JvmStatic
        fun getInstanceIfCreated(project: Project) = project.serviceIfCreated<LspSessionManager>()

        @JvmStatic
        fun getAllInstances() = ProjectManagerEx.getInstanceEx()
            .openProjects
            .mapNotNull { if (it.isDisposed) null else getInstance(it) }

        private val logger = logger<LspSessionManager>()
    }
}
