package info.voidev.lspidea.debug

import java.lang.reflect.Type

interface DebugTypeRenderer {

    /**
     * Type renderers with a higher priority will be run sooner.
     */
    val priority: Int get() = 0

    fun canRender(type: Class<*>, genericType: Type?): Boolean

    fun render(type: Class<*>, genericType: Type?, output: LspDebugTypeRendererOutput)
}
