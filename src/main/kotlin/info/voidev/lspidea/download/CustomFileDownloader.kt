package info.voidev.lspidea.download

import com.intellij.ide.IdeCoreBundle
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts.DialogTitle
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.util.SystemProperties
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.download.DownloadableFileDescription
import com.intellij.util.io.HttpRequests
import com.intellij.util.io.HttpRequests.RequestProcessor
import com.intellij.util.net.IOExceptionDialog
import org.jetbrains.annotations.Nls
import java.io.File
import java.io.IOException
import java.util.Collections
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.atomic.AtomicLong
import javax.swing.JComponent

@Deprecated("unused. DELETE")
class CustomFileDownloader(
    private val description: DownloadableFileDescription,
    private val myProject: Project?,
    private val myParentComponent: JComponent?,
    presentableDownloadName: String,
) {
    private var myDirectoryForDownloadedFilesPath: String? = null

    private val myMessage: @Nls(capitalization = Nls.Capitalization.Sentence) String
    private val myDialogTitle: @DialogTitle String

    init {
        myMessage = IdeCoreBundle.message("progress.download.0.title", presentableDownloadName)
        myDialogTitle =
            IdeCoreBundle.message("progress.download.0.title", StringUtil.capitalize(presentableDownloadName))
    }

    fun downloadWithProgress(
        targetDirectoryPath: String?,
        project: Project?,
        parentComponent: JComponent?,
    ): List<Pair<VirtualFile, DownloadableFileDescription>>? {
        val dir = if (targetDirectoryPath != null) {
            File(targetDirectoryPath)
        } else {
            val virtualDir = chooseDirectoryForFiles(project, parentComponent)
            if (virtualDir != null) {
                VfsUtilCore.virtualToIoFile(virtualDir)
            } else {
                return null
            }
        }

        return downloadWithProcess(dir, project, parentComponent)
    }

    fun downloadFilesWithProgress(
        targetDirectoryPath: String?,
        project: Project?,
        parentComponent: JComponent?,
    ): List<VirtualFile>? {
        return downloadWithProgress(targetDirectoryPath, project, parentComponent)
            ?.map { it.first }
    }

    fun downloadWithBackgroundProgress(
        targetDirectoryPath: String?,
        project: Project?,
    ): CompletableFuture<List<Pair<VirtualFile, DownloadableFileDescription>>?>? {
        val dir = if (targetDirectoryPath != null) {
            File(targetDirectoryPath)
        } else {
            val virtualDir = chooseDirectoryForFiles(project, null)
            if (virtualDir != null) {
                VfsUtilCore.virtualToIoFile(virtualDir)
            } else {
                return null
            }
        }

        return downloadWithBackgroundProcess(dir, project)
    }

    private fun downloadWithProcess(
        targetDir: File,
        project: Project?,
        parentComponent: JComponent?,
    ): List<Pair<VirtualFile, DownloadableFileDescription>>? {
        val localFiles = Ref.create<List<Pair<File, DownloadableFileDescription>>?>(null)
        val exceptionRef = Ref.create<IOException?>(null)
        val completed = ProgressManager.getInstance().runProcessWithProgressSynchronously({
            try {
                localFiles.set(download(targetDir))
            } catch (e: IOException) {
                exceptionRef.set(e)
            }
        }, myMessage, true, project, parentComponent)
        if (!completed) {
            return null
        }
        val exception = exceptionRef.get()
        if (exception != null) {
            val tryAgain = IOExceptionDialog.showErrorDialog(myDialogTitle, exception.message)
            return if (tryAgain) downloadWithProcess(targetDir, project, parentComponent) else null
        }

        return findVirtualFiles(localFiles.get())
    }

    private fun downloadWithBackgroundProcess(
        targetDir: File,
        project: Project?,
    ): CompletableFuture<List<Pair<VirtualFile, DownloadableFileDescription>>?> {
        val localFiles = Ref.create<List<Pair<File, DownloadableFileDescription>>?>(null)
        val exceptionRef = Ref.create<IOException?>(null)
        val result = CompletableFuture<List<Pair<VirtualFile, DownloadableFileDescription>>?>()
        ProgressManager.getInstance().run(object : Backgroundable(project, myMessage, true) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    localFiles.set(download(targetDir))
                } catch (exception: IOException) {
                    val tryAgain = IOExceptionDialog.showErrorDialog(myDialogTitle, exception.message)
                    if (tryAgain) {
                        downloadWithBackgroundProcess(
                            targetDir,
                            project
                        ).thenAccept { pairs: List<Pair<VirtualFile, DownloadableFileDescription>>? ->
                            result.complete(pairs)
                        }
                    }
                    result.complete(null)
                }
            }

            override fun onSuccess() {
                val files = localFiles.get()
                result.complete(if (files != null) findVirtualFiles(files) else null)
            }

            override fun onCancel() {
                result.complete(null)
            }
        })
        return result
    }

    @Throws(IOException::class)
    fun download(targetDir: File): List<Pair<File, DownloadableFileDescription>> {
        val downloadedFiles = Collections.synchronizedList(ArrayList<Pair<File, DownloadableFileDescription>>())
        val existingFiles = Collections.synchronizedList(ArrayList<Pair<File, DownloadableFileDescription>>())
        var parentIndicator = ProgressManager.getInstance().progressIndicator
        if (parentIndicator == null) {
            parentIndicator = EmptyProgressIndicator()
        } else {
            parentIndicator.isIndeterminate = false
        }
        return try {
            parentIndicator.text = IdeCoreBundle.message("progress.downloading.0.files.text", 1)

            val start = System.currentTimeMillis()

            val totalSize = AtomicLong()
            val result = AppExecutorUtil.getAppExecutorService().submit {
                ProgressManager.checkCanceled()

                val existing = File(targetDir, description.defaultFileName)

                val downloaded = try {
                    downloadFile(description, existing, ProgressManager.getGlobalProgressIndicator()!!)
                } catch (ex: IOException) {
                    throw IOException(
                        IdeCoreBundle.message("error.file.download.failed", description.downloadUrl, ex.message),
                        ex
                    )
                }
                if (FileUtil.filesEqual(downloaded, existing)) {
                    existingFiles.add(
                        Pair.create(
                            existing,
                            description
                        )
                    )
                } else {
                    totalSize.addAndGet(downloaded.length())
                    downloadedFiles.add(
                        Pair.create(
                            downloaded,
                            description
                        )
                    )
                }
            }

            try {
                result.get()
            } catch (e: InterruptedException) {
                throw ProcessCanceledException()
            } catch (e: ExecutionException) {
                if (e.cause is IOException) {
                    throw (e.cause as IOException?)!!
                }
                if (e.cause is ProcessCanceledException) {
                    throw (e.cause as ProcessCanceledException?)!!
                }
                LOG.error(e)
            }

            val tookMs = System.currentTimeMillis() - start

            LOG.debug(
                buildString {
                    append("Downloaded ")
                    append(StringUtil.formatFileSize(totalSize.get()))
                    append(" in ")
                    append(StringUtil.formatDuration(tookMs))
                    append(" (")
                    append(tookMs)
                    append("ms)")
                }
            )

            val localFiles: MutableList<Pair<File, DownloadableFileDescription>> = ArrayList()
            localFiles.addAll(moveToDir(downloadedFiles, targetDir))
            localFiles.addAll(existingFiles)
            localFiles
        } catch (e: ProcessCanceledException) {
            deleteFiles(downloadedFiles)
            throw e
        } catch (e: IOException) {
            deleteFiles(downloadedFiles)
            throw e
        }
    }

    fun download(): Array<VirtualFile>? {
        val files = downloadFilesWithProgress(myDirectoryForDownloadedFilesPath, myProject, myParentComponent)
        return if (files != null) VfsUtilCore.toVirtualFileArray(files) else null
    }

    companion object {
        private val LOG = logger<CustomFileDownloader>()

        private fun chooseDirectoryForFiles(project: Project?, parentComponent: JComponent?): VirtualFile? {
            val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
                .withTitle(IdeCoreBundle.message("dialog.directory.for.downloaded.files.title"))
                .withDescription(IdeCoreBundle.message("dialog.directory.for.downloaded.files.description"))
            val baseDir = project?.baseDir
            return FileChooser.chooseFile(descriptor, parentComponent, project, baseDir)
        }

        @Throws(IOException::class)
        private fun moveToDir(
            downloadedFiles: List<Pair<File, DownloadableFileDescription>>,
            targetDir: File,
        ): List<Pair<File, DownloadableFileDescription>> {
            FileUtil.createDirectory(targetDir)
            val result: MutableList<Pair<File, DownloadableFileDescription>> = ArrayList()
            for (pair in downloadedFiles) {
                val description = pair.getSecond()
                val fileName = description.generateFileName { s: String? ->
                    !File(
                        targetDir,
                        s
                    ).exists()
                }
                val toFile = File(targetDir, fileName)
                FileUtil.rename(pair.getFirst(), toFile)
                result.add(Pair.create(toFile, description))
            }
            return result
        }

        private fun findVirtualFiles(ioFiles: List<Pair<File, DownloadableFileDescription>>): List<Pair<VirtualFile, DownloadableFileDescription>> {
            val result: MutableList<Pair<VirtualFile, DownloadableFileDescription>> = ArrayList()
            for (pair in ioFiles) {
                val ioFile = pair.getFirst()
                val libraryRootFile = WriteAction.computeAndWait<VirtualFile?, RuntimeException> {
                    val url = VfsUtil.getUrlForLibraryRoot(ioFile)
                    LocalFileSystem.getInstance().refreshAndFindFileByIoFile(ioFile)
                    VirtualFileManager.getInstance().refreshAndFindFileByUrl(url)
                }
                if (libraryRootFile != null) {
                    result.add(Pair.create(libraryRootFile, pair.getSecond()))
                }
            }
            return result
        }

        private fun deleteFiles(pairs: List<Pair<File, DownloadableFileDescription>>) {
            for (pair in pairs) {
                FileUtil.delete(pair.getFirst())
            }
        }

        @Throws(IOException::class)
        private fun downloadFile(
            description: DownloadableFileDescription,
            existingFile: File,
            indicator: ProgressIndicator,
        ): File {
            val presentableUrl = description.presentableDownloadUrl

            indicator.text = IdeCoreBundle.message("progress.connecting.to.download.file.text", presentableUrl)

            val redirectLimit = SystemProperties.getIntProperty("idea.redirect.limit", 10)
            if (redirectLimit < 1) {
                LOG.warn("Download may fail because your redirect limit (idea.redirect.limit property) is set to $redirectLimit")
            }

            return HttpRequests
                .request(description.downloadUrl)
                .accept("application/octet-stream")
                .redirectLimit(redirectLimit)
                .connect(
                    RequestProcessor { request ->
                        // Don't download if it appears we already have the file
                        val size = request.connection.contentLength
                        if (existingFile.exists() && size.toLong() == existingFile.length()) {
                            return@RequestProcessor existingFile
                        }

                        indicator.text = IdeCoreBundle.message(
                            "progress.download.file.text",
                            description.presentableFileName,
                            presentableUrl
                        )

                        request.saveToFile(FileUtil.createTempFile("download.", ".tmp"), indicator)
                    }
                )
        }
    }
}
