package info.voidev.lspidea.debug

abstract class LspDebugValueRenderer<in T>(private val renderableClasses: Collection<Class<out @UnsafeVariance T>>) {

    open fun canRender(c: Class<*>) = c in renderableClasses

    fun render(value: T, output: LspDebugValueRendererOutput) {
        output.doRender(value)
    }

    protected abstract fun LspDebugValueRendererOutput.doRender(value: T)

    internal object Empty : LspDebugValueRenderer<Any?>(emptyList()) {
        override fun canRender(c: Class<*>) = true

        override fun LspDebugValueRendererOutput.doRender(value: Any?) {
        }
    }
}
