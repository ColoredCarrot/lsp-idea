package info.voidev.lspidea.debug

import com.intellij.xdebugger.frame.XValue
import com.intellij.xdebugger.frame.XValueNode
import com.intellij.xdebugger.frame.XValuePlace
import com.intellij.xdebugger.frame.presentation.XValuePresentation
import info.voidev.lspidea.util.reflect.relatedClass
import org.eclipse.lsp4j.jsonrpc.messages.Either
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

object DebugNodeFactory {

    /**
     * Creates a debug node for the given [value].
     *
     * If [type] is null, the [value]'s class is used for type information.
     * If both [type] and [value] are null, a generic "null" node is returned.
     */
    fun create(value: Any?, type: Type?): XValue {
        val ty = type ?: (value?.javaClass ?: return NullDebugNode)
        val c = ty.relatedClass

        return when {
            c != null && isCollectionType(c) -> CollectionDebugNode(ty, value)
            c == Either::class.java -> createForEither(value as Either<*, *>?, ty)
            else -> ObjectDebugNode(ty, value)
        }
    }

    private fun createForEither(value: Either<*, *>?, type: Type): XValue {
        // If the entire Either is null, show the full type union
        if (value == null) {
            return ObjectDebugNode(type, null)
        }

        val actualTypeArgs = (type as? ParameterizedType)?.actualTypeArguments
        return if (value.isLeft) {
            create(value.left, actualTypeArgs?.get(0))
        } else {
            create(value.right, actualTypeArgs?.get(1))
        }
    }

    private fun isCollectionType(rawType: Class<*>) =
        rawType.isArray || Collection::class.java.isAssignableFrom(rawType) && rawType.packageName.startsWith("java.")

    private object NullDebugNode : XValue() {
        override fun computePresentation(node: XValueNode, place: XValuePlace) {
            node.setPresentation(null, object : XValuePresentation() {
                override fun renderValue(renderer: XValueTextRenderer) {
                    renderer.renderKeywordValue("null")
                }
            }, false)
        }
    }
}
