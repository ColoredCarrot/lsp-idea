package info.voidev.lspidea.debug

//import com.intellij.xdebugger.frame.XCompositeNode
//import com.intellij.xdebugger.frame.XValue
//import com.intellij.xdebugger.frame.XValueChildrenList
//import com.intellij.xdebugger.frame.XValueNode
//import com.intellij.xdebugger.frame.XValuePlace
//import com.intellij.xdebugger.frame.presentation.XValuePresentation
//import org.eclipse.lsp4j.Position
//import org.eclipse.lsp4j.jsonrpc.validation.NonNull
//import org.jetbrains.annotations.NotNull
//import java.lang.reflect.Type
//
//class OLDLspXValue(private val type: Type, private val value: Any?, private val nonNull: Boolean) : XValue() {
//
//    private val props by lazy(LazyThreadSafetyMode.PUBLICATION) {
//
//    }
//
//    override fun computePresentation(node: XValueNode, place: XValuePlace) {
//        var typeString = LspDebugTypeRendererOutputImpl().append(type).finish()
//        if (!nonNull) {
//            typeString += '?'
//        }
//
//        val presentation = MyPresentation(typeString, value)
//        node.setPresentation(
//            null,
//            presentation,
//            value is Collection<*> || props.isNotEmpty()
//        )
//    }
//
//    override fun computeChildren(node: XCompositeNode) {
//        if (value == null) return super.computeChildren(node)
//
//        val props = props
//        if (props.isEmpty()) return super.computeChildren(node)
//
//        val children = XValueChildrenList(props.size)
//        for (prop in props) {
//            children.add(
//                prop.name,
//                OLDLspXValue(prop.type, prop.get(value), prop.isAnnotationPresent(NonNull::class.java) || prop.isAnnotationPresent(NotNull::class.java))
//            )
//        }
//        node.addChildren(children, true)
//    }
//
//    class MyPresentation(private val type: String, val value: Any?) : XValuePresentation() {
//        val usedRenderer by lazy {
//            if (value == null) null
//            else LspDebugRendererProvider.findValueRenderer(value.javaClass)
//        }
//
//        override fun getType() = type
//
//        override fun renderValue(renderer: XValueTextRenderer) {
//            if (value == null) {
//                renderer.renderKeywordValue("null")
//            } else {
//                LspDebugValueRendererOutputImpl(renderer).append(value)
//            }
//        }
//    }
//
//    companion object {
//        private const val JAVA_LANG_PACKAGE = "java.lang"
//        private val LSP4J_PACKAGE: String = Position::class.java.packageName
//    }
//}
