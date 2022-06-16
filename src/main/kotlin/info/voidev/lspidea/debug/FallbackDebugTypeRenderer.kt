package info.voidev.lspidea.debug

import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

object FallbackDebugTypeRenderer : DebugTypeRenderer {

    override val priority get() = -10_000_000

    override fun canRender(type: Class<*>, genericType: Type?) = true

    override fun render(type: Class<*>, genericType: Type?, output: LspDebugTypeRendererOutput) {
        if (type.isArray) {
            renderArray(type, genericType, output)
            return
        }

        if (genericType is ParameterizedType) {
            renderParameterized(genericType, output)
            return
        }

        output.append(genericType?.typeName ?: type.name)
    }

    private fun renderArray(type: Class<*>, genericType: Type?, output: LspDebugTypeRendererOutput) {
        output.append((genericType as? GenericArrayType)?.genericComponentType ?: type.componentType)
        output.append("[]")
    }

    private fun renderParameterized(genericType: ParameterizedType, output: LspDebugTypeRendererOutput) {
        output.append(genericType.rawType)

        val typeArgs = genericType.actualTypeArguments
        if (typeArgs.isNullOrEmpty()) {
            return
        }

        output.append("<")
        typeArgs.forEachIndexed { i, typeArg ->
            if (i > 0) output.append(", ")
            output.append(typeArg)
        }
        output.append(">")
    }

}
