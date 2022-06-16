package info.voidev.lspidea.debug

import com.intellij.xdebugger.frame.XCompositeNode
import com.intellij.xdebugger.frame.XValue
import com.intellij.xdebugger.frame.XValueChildrenList
import com.intellij.xdebugger.frame.XValueNode
import com.intellij.xdebugger.frame.XValuePlace
import com.intellij.xdebugger.frame.presentation.XValuePresentation
import info.voidev.lspidea.util.reflect.BeanProperties
import info.voidev.lspidea.util.reflect.relatedClass
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable

class ObjectDebugNode(private val type: Type, private val value: Any?) : XValue() {

    private val rawType = type.relatedClass

    override fun computePresentation(node: XValueNode, place: XValuePlace) {
        if (rawType == null) {
            // We will not be able to discover child properties
            node.setPresentation(null, type.renderForLspDebug(), "", false)
            return
        }

        val hasChildren = value != null && BeanProperties[rawType].isNotEmpty()

        node.setPresentation(null, MyPresentation(type.renderForLspDebug(), value), hasChildren)
    }

    override fun computeChildren(node: XCompositeNode) {
        if (rawType == null || value == null) {
            node.addChildren(XValueChildrenList.EMPTY, true)
            return
        }

        val props = BeanProperties[rawType]
        val children = XValueChildrenList(props.size)

        for (prop in props) {
            val propValue = prop.get(value)
            var propType = prop.type

            // If our type is a ParameterizedType (e.g. Either<Int, String>),
            //  then prop.type can be a TypeVariable referencing one of the types
            //  from our type parameter list;
            //  we can resolve it in that case.
            // Note: There may be a more complex cas with type variables coming from an enclosing class,
            //  which is not handled here.
            if (type is ParameterizedType && propType is TypeVariable<*>) {
                val typeParamIdx = propType.genericDeclaration.typeParameters.indexOf(propType)
                propType = type.actualTypeArguments[typeParamIdx]
            }

            children.add(prop.name, DebugNodeFactory.create(propValue, propType))
        }

        node.addChildren(children, true)
    }

    private class MyPresentation(private val typeString: String?, private val value: Any?) : XValuePresentation() {

        override fun getType() = typeString

        override fun renderValue(renderer: XValueTextRenderer) {
            LspDebugValueRendererOutputImpl(renderer).append(value)
        }

    }
}
