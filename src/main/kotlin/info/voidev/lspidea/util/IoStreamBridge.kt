package info.voidev.lspidea.util

import java.io.InputStream
import java.io.OutputStream

class IoStreamBridge(private val src: InputStream, private val sink: OutputStream) {

    private val buf = ByteArray(512)

    fun transferAvailable() {
        var available = src.available()
        while (available > 0) {
            val read = src.read(buf, 0, minOf(available, buf.size))
            sink.write(buf, 0, read)

            available = src.available()
        }
        sink.flush()
    }

}
