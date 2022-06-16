package info.voidev.lspidea.connect

import java.io.InputStream
import java.io.OutputStream

class LspConnection(val s2c: InputStream, val c2s: OutputStream, val s2cInfo: InputStream) {
    operator fun component1() = s2c
    operator fun component2() = c2s
    operator fun component3() = s2cInfo
}
