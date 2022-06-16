package info.voidev.lspidea

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import info.voidev.lspidea.misc.LspNotification
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError

object LspIdea {

    val BASE_PACKAGE_NAME: String = javaClass.packageName

    const val EP_PREFIX = "info.voidev.lspidea."

    val logger = Logger.getInstance(javaClass)

    fun showResponseError(title: String, error: ResponseError, project: Project?) {
        LspNotification.group()
            .createNotification(title, error.message ?: "Error code ${error.code}", NotificationType.ERROR)
            .notify(project)

        logger.info("Got response error from language server: $error", responseErrorStacktrace())
    }

    fun showWarning(content: String, project: Project?) {
        LspNotification.group()
            .createNotification(content, NotificationType.WARNING)
            .notify(project)

        logger.warn(content)
    }

    fun showError(content: String, project: Project?) {
        LspNotification.group()
            .createNotification(content, NotificationType.ERROR)
            .notify(project)

        logger.warn(content)
    }

    fun showError(title: String, content: String, project: Project?) {
        LspNotification.group()
            .createNotification(title, content, NotificationType.ERROR)
            .notify(project)

        logger.warn("$title | $content")
    }

    fun showMessage(content: String, project: Project) {
        LspNotification.group()
            .createNotification(content, NotificationType.INFORMATION)
            .notify(project)
    }

    private fun responseErrorStacktrace() = if (RESPONSE_ERROR_STACKTRACE) Throwable() else null

    private const val RESPONSE_ERROR_STACKTRACE = true

    val thePluginId get() = PluginId.findId("info.voidev.lspidea")!!

    val thePlugin get() = PluginManagerCore.getPlugin(thePluginId)!!
}
