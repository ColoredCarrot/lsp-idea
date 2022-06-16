package info.voidev.lspidea.debug

import com.intellij.openapi.extensions.ExtensionPointName
import info.voidev.lspidea.LspIdea
import java.lang.reflect.Type

abstract class LspDebugRendererProvider {

    private val _valueRenderers = ArrayList<LspDebugValueRenderer<*>>()
    private val _typeRenderers = ArrayList<DebugTypeRenderer>()

    open val valueRenderers: Collection<LspDebugValueRenderer<*>> get() = _valueRenderers
    open val typeRenderers: Collection<DebugTypeRenderer> get() = _typeRenderers

    protected fun addValueRenderer(renderer: LspDebugValueRenderer<*>) {
        _valueRenderers += renderer
    }

    protected fun addTypeRenderer(typeRenderer: DebugTypeRenderer) {
        _typeRenderers += typeRenderer
    }

    protected inline fun <reified T : Any> addValueRenderer(crossinline block: LspDebugValueRendererOutput.(value: T) -> Unit) {
        val asPrim = T::class.javaPrimitiveType
        val asObj = T::class.javaObjectType
        val classes =
            if (asPrim == null || asObj == asPrim) listOf(T::class.java)
            else listOf(asObj, asPrim)

        addValueRenderer(object : LspDebugValueRenderer<T>(classes) {
            override fun LspDebugValueRendererOutput.doRender(value: T) {
                block(value)
            }
        })
    }

    protected inline fun <reified T : Any> addTypeRenderer(crossinline block: LspDebugTypeRendererOutput.(genericType: Type?) -> Unit) {
        addTypeRenderer(object : DebugTypeRenderer {
            override fun canRender(type: Class<*>, genericType: Type?) = type == T::class.java

            override fun render(type: Class<*>, genericType: Type?, output: LspDebugTypeRendererOutput) {
                output.block(genericType)
            }
        })
    }

    companion object {
        @JvmStatic
        val EP_NAME =
            ExtensionPointName.create<LspDebugRendererProvider>(LspIdea.EP_PREFIX + "debugRendererProvider")

        @JvmStatic
        fun <T> findValueRenderer(c: Class<T>): LspDebugValueRenderer<T> {
            for (provider in EP_NAME.extensionList) {
                for (renderer in provider.valueRenderers) {
                    if (renderer.canRender(c)) {
                        @Suppress("UNCHECKED_CAST")
                        return renderer as LspDebugValueRenderer<T>
                    }
                }
            }
            return LspDebugValueRenderer.Empty
        }

        @JvmStatic
        fun findTypeRenderer(type: Class<*>, genericType: Type?): DebugTypeRenderer {
            return EP_NAME.extensionList
                .asSequence()
                .flatMap { it.typeRenderers }
                .filter { it.canRender(type, genericType) }
                .maxByOrNull { it.priority }
                ?: FallbackDebugTypeRenderer
        }
    }
}
