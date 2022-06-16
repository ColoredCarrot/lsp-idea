package info.voidev.lspidea.debug

import com.intellij.xdebugger.frame.XCompositeNode
import com.intellij.xdebugger.frame.XStackFrame
import com.intellij.xdebugger.frame.XValue
import com.intellij.xdebugger.frame.XValueChildrenList

class DebugNodeXStackFrame(private val root: XValue) : XStackFrame() {

    override fun computeChildren(node: XCompositeNode) {
        val children = XValueChildrenList(1)
        children.add("<root>", root)
        node.addChildren(children, true)
    }
}
