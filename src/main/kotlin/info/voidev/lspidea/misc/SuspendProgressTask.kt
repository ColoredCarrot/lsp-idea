package info.voidev.lspidea.misc

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.annotations.Nls
import java.util.concurrent.CompletableFuture

fun <T> ProgressManager.runBackgroundable(
    @Nls(capitalization = Nls.Capitalization.Sentence) title: String,
    project: Project?,
    action: suspend CoroutineScope.() -> T,
): CompletableFuture<T> {
    val future = CompletableFuture<T>()
    run(object : Task.Backgroundable(project, title, true) {
        override fun run(indicator: ProgressIndicator) {
            try {
                future.complete(runBlockingCancellable(indicator, action))
            } catch (ex: Exception) {
                future.completeExceptionally(ex)
            }
        }
    })
    return future
}
