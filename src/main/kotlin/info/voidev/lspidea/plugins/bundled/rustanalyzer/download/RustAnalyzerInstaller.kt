package info.voidev.lspidea.plugins.bundled.rustanalyzer.download

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.io.HttpRequests
import com.intellij.util.io.createDirectories
import com.intellij.util.io.inputStream
import com.intellij.util.io.outputStream
import com.intellij.util.net.IOExceptionDialog
import com.intellij.util.text.CharSequenceReader
import info.voidev.lspidea.download.Downloader
import info.voidev.lspidea.download.LspServerExecutableInstaller
import info.voidev.lspidea.github.GitHubApiEndpoints
import info.voidev.lspidea.github.dto.GitHubReleaseDto
import info.voidev.lspidea.misc.LspServerExecutableUtil
import info.voidev.lspidea.util.joinUnwrapExceptionsCancellable
import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.zip.GZIPInputStream
import javax.swing.JComponent

class RustAnalyzerInstaller : LspServerExecutableInstaller {
    companion object {
        private val gson = Gson().newBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

        private val gitHubReleasesDtoType = object : TypeToken<List<GitHubReleaseDto>>() {}.type

        private const val WINDOWS_RELEASE_TAG_NAME = "rust-analyzer-x86_64-pc-windows-msvc.gz"
        private const val WINDOWS_DOWNLOADED_FILE_NAME = "rust-analyzer-x86_64-pc-windows-msvc.gz"
        private const val WINDOWS_EXECUTABLE_FILE_NAME = "rust-analyzer-x86_64-pc-windows-msvc.exe"

        private val logger = logger<RustAnalyzerInstaller>()

        private fun getDefaultInstallDir(): Path? {
            val lspidea = when {
                SystemInfo.isWindows -> (System.getenv("LOCALAPPDATA") ?: System.getenv("APPDATA"))?.let { Path.of(it, "LSP-IDEA") }
                SystemInfo.isMac -> System.getProperty("user.home")?.let { Path.of(it, ".lspidea") }
                SystemInfo.isLinux -> System.getProperty("user.home")?.let { Path.of(it, ".lspidea") }
                else -> null
            }
            return lspidea?.resolve("language-servers")
        }
    }

    override fun download(uiContext: JComponent): CompletionStage<Path>? {
        ApplicationManager.getApplication().assertIsDispatchThread()

        if (!SystemInfo.isWindows) {
            Messages.showErrorDialog(uiContext, "Automatic installation is currently only supported for Windows")
            return null
        }

        val releasesJson = ProgressManager.getInstance()
            .run(object : Task.WithResult<CharSequence?, Exception>(null, "Fetching", true) {
                override fun compute(indicator: ProgressIndicator): CharSequence? {
                    return try {
                        AppExecutorUtil.getAppExecutorService().submit(
                            Callable {
                                HttpRequests
                                    .request(GitHubApiEndpoints.releases("rust-lang", "rust-analyzer", 5))
                                    .accept("application/json")
                                    .productNameAsUserAgent()
                                    .readTimeout(5000)
                                    .readChars()
                            }
                        ).joinUnwrapExceptionsCancellable(indicator = indicator)
                    } catch (ex: ProcessCanceledException) {
                        throw ex
                    } catch (ex: Exception) {
                        Messages.showErrorDialog(uiContext, ex.message ?: "No further details")
                        return null
                    }
                }
            }) ?: return null

        val releasesDto = gson.fromJson<List<GitHubReleaseDto>>(CharSequenceReader(releasesJson), gitHubReleasesDtoType)

        val actionGroup = DefaultActionGroup()
        actionGroup.isPopup = true

        val future = CompletableFuture<Path>()

        actionGroup.addAll(
            releasesDto.mapNotNull { releaseDto ->
                val theAsset = releaseDto.assets.firstOrNull { it.name == WINDOWS_RELEASE_TAG_NAME }

                if (theAsset == null) null
                else DownloadExecutableAction(releaseDto.tagName, theAsset.url, uiContext, future)
            }
        )

        val popup = JBPopupFactory.getInstance().createActionGroupPopup(
            null,
            actionGroup,
            DataManager.getInstance().getDataContext(uiContext),
            JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
            true
        )
        popup.showUnderneathOf(uiContext)

        return future
    }

    private class DownloadExecutableAction(
        private val name: String,
        private val url: String,
        private val context: JComponent,
        private val callback: CompletableFuture<Path>,
    ) : AnAction(name) {

        override fun actionPerformed(e: AnActionEvent) {
            val dest = FileUtil.createTempFile("extract-", "-$WINDOWS_DOWNLOADED_FILE_NAME", true).toPath()

            val success = ProgressManager.getInstance()
                .run(object : Task.WithResult<Boolean, Exception>(null, "Downloading rust-analyzer $name", true) {
                    override fun compute(indicator: ProgressIndicator): Boolean {
                        return Downloader.download(
                            url,
                            dest,
                            "rust-analyzer $name",
                            indicator
                        )
                    }
                })

            if (!success) return

            // rust-analyzer releases are packaged in a .gz file
            extractDownloadedFile(dest)
        }

        private fun extractDownloadedFile(zippedFile: Path) {

            val dir = getDefaultInstallDir()
            if (dir == null) {
                Messages.showErrorDialog(context, "Unknown plugin path", "Cannot Download")
                return
            }

            dir.createDirectories()

            val dest = dir.resolve(WINDOWS_EXECUTABLE_FILE_NAME)

            val title = "Extracting"

            val finalPath = ProgressManager.getInstance()
                .run(object : Task.WithResult<Path?, Exception>(null, title, true) {
                    override fun compute(indicator: ProgressIndicator): Path? {
                        while (true) {
                            indicator.checkCanceled()
                            try {
                                doExtract(zippedFile, dest, indicator)
                                return dest
                            } catch (ex: IOException) {
                                val tryAgain = IOExceptionDialog.showErrorDialog(title, ex.message)
                                if (!tryAgain) {
                                    return null
                                }
                            }
                        }
                    }
                })
                ?: return

            try {
                FileUtil.delete(zippedFile)
            } catch (ex: IOException) {
                logger.info("Failed to delete zipped file", ex)
            }

            if (!LspServerExecutableUtil.isPotentiallyValidServerExecutable(dest)) {
                invokeLater { Messages.showErrorDialog(context, "The executable appears to be invalid.", title) }
                return
            }

            callback.complete(finalPath)
        }

        @Throws(IOException::class)
        private fun doExtract(path: Path, dest: Path, indicator: ProgressIndicator) {
            indicator.checkCanceled()
            GZIPInputStream(path.inputStream().buffered()).use { input ->
                indicator.checkCanceled()

                dest.outputStream().use { output ->
                    indicator.checkCanceled()

                    val buf = ByteArray(4096)

                    var read = input.read(buf)
                    while (read > 0) {
                        indicator.checkCanceled()

                        output.write(buf, 0, read)
                        read = input.read(buf)
                    }
                }
            }
        }
    }
}
