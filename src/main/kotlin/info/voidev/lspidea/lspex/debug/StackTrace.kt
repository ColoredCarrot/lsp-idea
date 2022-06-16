package info.voidev.lspidea.lspex.debug

import org.jetbrains.annotations.Contract

class StackTrace(val elements: Array<StackTraceElement>) {
    companion object {
        fun capture() = StackTrace(Throwable().stackTrace)

        @Contract("true -> new; false -> null", pure = true)
        fun captureIf(condition: Boolean) = if (condition) capture() else null
    }
}
