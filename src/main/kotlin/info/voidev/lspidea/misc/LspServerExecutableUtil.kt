package info.voidev.lspidea.misc

import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.io.inputStream
import java.nio.file.Path

object LspServerExecutableUtil {

    fun isPotentiallyValidServerExecutable(path: Path): Boolean {
        return try {
            when {
                SystemInfo.isWindows -> windows(path)
                else -> true
            }
        } catch (ex: ProcessCanceledException) {
            throw ex
        } catch (ex: Exception) {
            return false
        }
    }

    private fun windows(path: Path): Boolean {
        val firstTwoBytes = path.inputStream().use { it.readNBytes(2) }
        return firstTwoBytes[0] == 'M'.code.toByte() && firstTwoBytes[1] == 'Z'.code.toByte()
    }
}
