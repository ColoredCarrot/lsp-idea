package info.voidev.lspidea.ui

import com.intellij.openapi.diagnostic.logger
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.LayoutManager2

/**
 * A simple layout manager that manages
 * a single base component
 * that spans the entire space
 * and any number of additional overlay components
 * that are positioned absolutely on top of the base component.
 */
class OverlayLayout : LayoutManager2 {

    private var baseComponent: Component? = null

    private val overlayComps = ArrayList<Pair<Component, Overlay>>()

    override fun addLayoutComponent(comp: Component, constraints: Any?) {
        val layer = (constraints ?: Base) as? Constraints
            ?: throw IllegalArgumentException("Constraint must be of type Constraints, but is $constraints")

        when (layer) {
            Base -> {
                logger.assertTrue(baseComponent == null, "Overriding the base component is likely a mistake")
                baseComponent = comp
            }
            is Overlay -> {
                overlayComps += comp to layer
            }
        }
    }

    override fun addLayoutComponent(name: String?, comp: Component) =
        addLayoutComponent(comp, name)

    override fun removeLayoutComponent(comp: Component?) {
        comp ?: return
        if (baseComponent == comp) {
            baseComponent = null
        } else {
            overlayComps.removeIf { it.first == comp }
        }
    }

    override fun preferredLayoutSize(parent: Container): Dimension {
        val dim = baseComponent?.preferredSize ?: Dimension()

        val insets = parent.insets
        dim.width += insets.left + insets.right
        dim.height += insets.top + insets.bottom

        return dim
    }

    override fun minimumLayoutSize(parent: Container): Dimension {
        val dim = baseComponent?.minimumSize ?: Dimension()

        val insets = parent.insets
        dim.width += insets.left + insets.right
        dim.height += insets.top + insets.bottom

        return dim
    }

    override fun maximumLayoutSize(target: Container?) =
        baseComponent?.maximumSize ?: Dimension(Int.MAX_VALUE, Int.MAX_VALUE)

    override fun getLayoutAlignmentX(target: Container?) = 0.5f

    override fun getLayoutAlignmentY(target: Container?) = 0.5f

    override fun invalidateLayout(target: Container?) {
    }

    override fun layoutContainer(parent: Container) {
        val insets = parent.insets
        val top = insets.top
        val bottom = parent.height - insets.bottom
        val left = insets.left
        val right = parent.width - insets.right

        baseComponent?.also { base ->
            parent.setComponentZOrder(base, overlayComps.size)
            base.setBounds(left, top, right - left, bottom - top)
        }

        overlayComps.forEachIndexed { idx, (overlayComp, constraints) ->
            parent.setComponentZOrder(overlayComp, idx)
            val y = (top + constraints.yOff).coerceAtMost(bottom)
            overlayComp.setBounds(left, y, right - left, minOf(bottom - y, overlayComp.preferredSize.height))
        }
    }

    sealed class Constraints
    object Base : Constraints()
    data class Overlay(val yOff: Int) : Constraints()

    companion object {
        private val logger = logger<OverlayLayout>()
    }
}
