package info.voidev.lspidea.misc

import java.io.Writer

class LineBasedWriter(
    lock: Any = Any(),
    private val closeFn: (() -> Unit)? = null,
    private val writeLineFn: (String) -> Unit,
) : Writer(lock) {

    private val buf = StringBuilder()

    override fun write(cbuf: CharArray, off: Int, len: Int) {
        // Scan for newlines
        var i = 0
        while (i < len) {
            val c = off + i

            if (cbuf[c] == '\n') {
                newline()
            } else if (cbuf[c] == '\r') {
                // ignore
            } else {
                buf.append(cbuf[c])
            }

            ++i
        }
    }

    private fun newline() {
        if (buf.isNotBlank()) {
            writeLineFn(buf.toString())
        }
        buf.clear()
    }

    override fun close() {
        closeFn?.invoke()
    }

    override fun flush() {
        // Do nothing
        // We may drop characters if a newline is not written; that is the caller's responsibility
    }
}
