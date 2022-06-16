package info.voidev.lspidea.files

import com.intellij.openapi.Disposable
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFile
import info.voidev.lspidea.util.identifyForLsp
import java.util.concurrent.atomic.AtomicInteger

/**
 * A document for which we have sent a didOpen notification to the language server.
 *
 * Always associated with exactly one language server
 * (in the future, we might support multiple LspOpenDocument instances per file for different language servers).
 */
class LspOpenDocument(val file: VirtualFile) : Disposable {

    private val _version = AtomicInteger(1)

    val identifier = file.identifyForLsp()

    val document get() = FileDocumentManager.getInstance().getDocument(file)!!

    val version get() = _version.get()

    fun incrementVersion(): Int {
        return _version.incrementAndGet()
    }

    override fun dispose() {
        // Do nothing; we just implement Disposable so others can hook onto our dispose()
    }
}
