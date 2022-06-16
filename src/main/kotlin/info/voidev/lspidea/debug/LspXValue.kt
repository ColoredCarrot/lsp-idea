package info.voidev.lspidea.debug

import com.intellij.xdebugger.frame.XValue
import com.intellij.xdebugger.frame.XValueNode
import com.intellij.xdebugger.frame.XValuePlace
import com.intellij.xdebugger.frame.presentation.XValuePresentation
import com.intellij.xdebugger.frame.presentation.XValuePresentation.XValueTextRenderer
import java.lang.reflect.Type
import javax.swing.Icon

abstract class XValueBase : XValue() {

    protected abstract val hasChildren: Boolean

    protected open fun getPresentation(): XValuePresentationBase {
        throw NotImplementedError("override either getPresentation or computePresentation")
    }

    override fun computePresentation(node: XValueNode, place: XValuePlace) {
        val p = getPresentation()
        node.setPresentation(p.icon, p, hasChildren)
    }

    // true by default; we don't support any navigation
    override fun canNavigateToSource() = false
}

sealed class LspXValue(protected val type: Type) : XValue()

inline fun makeXValuePresentation(typeString: String? = null, renderFn: (renderer: XValueTextRenderer) -> Unit) =
    object : XValuePresentationBase() {

        override fun getType() = typeString

        override fun renderValue(renderer: XValueTextRenderer) {
        }
    }

abstract class XValuePresentationBase : XValuePresentation() {
    open val icon: Icon? get() = null
}

class LspXValuePresentation(type: Type, private val value: Any?) : XValuePresentation() {

    private val typeString = type.renderForLspDebug().ifEmpty { null }

    override fun renderValue(renderer: XValueTextRenderer) {
        LspDebugValueRendererOutputImpl(renderer).append(value)
    }

    override fun getType() = typeString
}

class LspCollectionXValuePresentation(itemType: Type, private val value: Collection<*>) : XValuePresentation() {
    private val typeString = "...[${value.size}]"

    override fun renderValue(renderer: XValueTextRenderer) {
    }
}
