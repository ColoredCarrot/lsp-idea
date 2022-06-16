package info.voidev.lspidea.download

import com.intellij.ide.IdeCoreBundle
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.SystemProperties
import com.intellij.util.io.HttpRequests
import com.intellij.util.net.IOExceptionDialog
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.deleteIfExists

object Downloader {

    fun download(url: String, dest: Path, presentableDownloadName: String, indicator: ProgressIndicator?): Boolean {
        logger.assertTrue(dest.fileSystem == FileSystems.getDefault())

        val title = IdeCoreBundle.message("progress.download.0.title", presentableDownloadName)

        val tempFile = FileUtil.createTempFile(dest.parent.toFile(), "download.", ".tmp")

        var success = false
        var tryAgain = false
        do {
            try {
                indicator?.checkCanceled()
                doDownload(url, tempFile.toPath(), indicator)
                success = true
            } catch (ex: IOException) {
                tryAgain = IOExceptionDialog.showErrorDialog(title, ex.message)
            }
        } while (tryAgain)

        if (!success) {
            return false
        }

        indicator?.checkCanceled()

        try {
            dest.deleteIfExists()
        } catch (ex: IOException) {
            invokeLater { Messages.showErrorDialog("Failed to overwrite existing file: ${ex.message}", title) }
            return false
        }

        try {
            FileUtil.rename(tempFile, dest.toFile())
        } catch (ex: IOException) {
            invokeLater { Messages.showErrorDialog(ex.message, title) }
            return false
        }

        return true
    }

    @Throws(IOException::class)
    private fun doDownload(url: String, dest: Path, indicator: ProgressIndicator?) {
        indicator?.checkCanceled()

        val redirectLimit = SystemProperties.getIntProperty("idea.redirect.limit", 10)
        if (redirectLimit < 1) {
            logger.warn("Download may fail because your redirect limit (idea.redirect.limit property) is set to $redirectLimit")
        }

        HttpRequests
            .request(url)
            .accept("application/octet-stream")
            .productNameAsUserAgent()
            .readTimeout(5000)
            .redirectLimit(redirectLimit)
            .saveToFile(dest, indicator)
    }

    private val logger = logger<Downloader>()
}
