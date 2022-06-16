package info.voidev.lspidea.misc

import com.intellij.openapi.diagnostic.Logger

fun Logger.traceWriter() = LineBasedWriter { line -> trace(line) } // TODO change to trace
