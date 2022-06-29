package info.voidev.lspidea.debug

// import com.intellij.xdebugger.frame.XCompositeNode
// import com.intellij.xdebugger.frame.XValue
// import com.intellij.xdebugger.frame.XValueChildrenList
// import com.intellij.xdebugger.frame.XValueNode
// import com.intellij.xdebugger.frame.XValuePlace
// import com.intellij.xdebugger.frame.presentation.XValuePresentation
// import info.voidev.lspidea.util.reflect.getWrapperClass
//
// class LspXCollectionValue(private val theCollection: Collection<*>) : XValue() {
//
//    // Simplistic approach not considering interfaces
//    private val itemClass = theCollection.asSequence()
//        .mapNotNull { it?.javaClass }
//        .reduceOrNull(::commonSuperclass)
//
//    override fun computePresentation(node: XValueNode, place: XValuePlace) {
//        node.setPresentation(null, MyPresentation(), theCollection.isNotEmpty())
//    }
//
//    override fun computeChildren(node: XCompositeNode) {
//        if (theCollection.isEmpty()) {
//            return super.computeChildren(node)
//        }
//
//        val children = XValueChildrenList(theCollection.size)
//
//        for ((index, item) in theCollection.withIndex()) {
//            children.add("[$index]", LspXValueFactory.make(item))
//        }
//
//        node.addChildren(children, true)
//    }
//
//    private inner class MyPresentation : XValuePresentation() {
//        override fun getType() = if (itemClass != null) itemClass.name + "[]" else "[]"
//
//        override fun renderValue(renderer: XValueTextRenderer) {
//            renderer.renderValue(if (theCollection.size == 1) "1 item" else "${theCollection.size} items")
//        }
//    }
//
//    companion object {
//        private fun commonSuperclass(a: Class<*>, b: Class<*>): Class<*> {
//            val aw = a.wrapperClass
//            val bw = b.wrapperClass
//
//            var result = aw
//            while (!result.isAssignableFrom(bw)) {
//                result = result.superclass ?: return Any::class.java
//            }
//            return result
//        }
//    }
// }
