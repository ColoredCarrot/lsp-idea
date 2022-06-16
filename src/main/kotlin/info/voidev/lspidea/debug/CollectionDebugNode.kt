package info.voidev.lspidea.debug

import com.intellij.xdebugger.frame.XCompositeNode
import com.intellij.xdebugger.frame.XValue
import com.intellij.xdebugger.frame.XValueChildrenList
import com.intellij.xdebugger.frame.XValueNode
import com.intellij.xdebugger.frame.XValuePlace
import com.intellij.xdebugger.frame.presentation.XValuePresentation
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class CollectionDebugNode(private val type: Type, private val theCollection: Any?) : XValue() {

    private var fastItemCount: Int? = null

    private val itemType = when (type) {
        is ParameterizedType -> type.actualTypeArguments.single()
        is GenericArrayType -> type.genericComponentType
        is Class<*> -> type.componentType // null if not an array type, but a collection class without generic information
        else -> null // weird, this should never happen under normal circumstances
    }

    override fun computePresentation(node: XValueNode, place: XValuePlace) {
        val typeString = getTypeString()

        // getTypeString also computes the item count, so we can use it here
        val hasChildren = fastItemCount?.let { it > 0 }
            ?: (theCollection as? Iterable<*>)?.iterator()?.hasNext()
            ?: false

        node.setPresentation(null, MyPresentation(typeString, theCollection == null), hasChildren)
    }

    private fun computeItemCountFast() {
        fastItemCount = when (theCollection) {
            is Collection<*> -> theCollection.size
            // Note the omission of the Iterable<*> case

            is Array<*> -> theCollection.size

            is BooleanArray -> theCollection.size
            is CharArray -> theCollection.size
            is ByteArray -> theCollection.size
            is ShortArray -> theCollection.size
            is IntArray -> theCollection.size
            is LongArray -> theCollection.size
            is FloatArray -> theCollection.size
            is DoubleArray -> theCollection.size

            null -> 0

            else -> null
        }
    }

    private fun getTypeString(): String {
        computeItemCountFast()

        if (itemType == null && fastItemCount == null) {
            return "array"
        }

        val itemTypeString = itemType?.renderForLspDebug() ?: ""
        val itemCountString = fastItemCount?.toString()?.takeUnless { theCollection == null } ?: ""

        return "$itemTypeString[$itemCountString]"
    }

    private class MyPresentation(private val typeString: String?, private val isNull: Boolean) : XValuePresentation() {
        override fun getType() = typeString

        override fun renderValue(renderer: XValueTextRenderer) {
            if (isNull) {
                renderer.renderKeywordValue("null")
            }
        }
    }

    override fun computeChildren(node: XCompositeNode) {
        // We haven't converted the collection to a List yet
        // in order to avoid the overhead of boxing the primitives in an IntArray, for example.
        // But here we no longer care...
        // TODO: If item count is very large, switch to background thread

        val children = fastItemCount?.let { XValueChildrenList(it) } ?: XValueChildrenList()

        getValues()
            .map { DebugNodeFactory.create(it, itemType) }
            .forEachIndexed { index, item -> children.add("[$index]", item) }

        node.addChildren(children, true)
    }

    private fun getValues(): Sequence<Any?> =
        when (theCollection) {
            is Collection<*> -> theCollection.asSequence()
            is Iterable<*> -> theCollection.asSequence()

            is Array<*> -> theCollection.asSequence()

            is BooleanArray -> theCollection.asSequence()
            is CharArray -> theCollection.asSequence()
            is ByteArray -> theCollection.asSequence()
            is ShortArray -> theCollection.asSequence()
            is IntArray -> theCollection.asSequence()
            is LongArray -> theCollection.asSequence()
            is FloatArray -> theCollection.asSequence()
            is DoubleArray -> theCollection.asSequence()

            null -> emptySequence()

            else -> throw IllegalStateException("Illegal collection: $theCollection ($type)")
        }
}
