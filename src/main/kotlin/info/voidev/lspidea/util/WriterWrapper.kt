package info.voidev.lspidea.util

import java.io.Writer

class WriterWrapper(var delegate: Writer? = null) : Writer() {

    init {
        //FIXME remove
//        delegate = System.err.writer()
    }

    override fun close() {
        delegate?.close()
    }

    override fun flush() {
        delegate?.flush()
    }

    override fun write(cbuf: CharArray, off: Int, len: Int) {
        delegate?.write(cbuf, off, len)
    }

    override fun write(c: Int) {
        delegate?.write(c)
    }

    override fun write(cbuf: CharArray) {
        delegate?.write(cbuf)
    }

    override fun write(str: String) {
        delegate?.write(str)
    }

    override fun write(str: String, off: Int, len: Int) {
        delegate?.write(str, off, len)
    }

    override fun append(csq: CharSequence?): Writer {
        delegate = delegate?.append(csq)
        return this
    }

    override fun append(csq: CharSequence?, start: Int, end: Int): Writer {
        delegate = delegate?.append(csq, start, end)
        return this
    }

    override fun append(c: Char): Writer {
        delegate = delegate?.append(c)
        return this
    }
}
