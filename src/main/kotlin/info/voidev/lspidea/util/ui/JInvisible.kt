package info.voidev.lspidea.util.ui

import java.awt.Component
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JComponent

/**
 * A [Component] wrapper that delegates all size- and layout related methods to [delegate]
 * and overrides [paint] to a NOP.
 */
@Suppress("HasPlatformType")
open class JInvisible(private val delegate: Component) : JComponent() {

    override fun paint(g: Graphics?) {
    }

    @Deprecated("Deprecated in Java")
    override fun size() = delegate.size()

    override fun getSize() = delegate.size

    override fun getSize(rv: Dimension?) = delegate.getSize(rv)

    override fun setSize(d: Dimension?) {
        delegate.size = d
    }

    override fun setSize(width: Int, height: Int) = delegate.setSize(width, height)

    @Deprecated("Deprecated in Java")
    override fun minimumSize() = delegate.minimumSize()

    override fun getMinimumSize() = delegate.minimumSize

    override fun setMinimumSize(minimumSize: Dimension?) {
        delegate.minimumSize = minimumSize
    }

    override fun isMinimumSizeSet() = delegate.isMinimumSizeSet

    override fun getMaximumSize() = delegate.maximumSize

    override fun setMaximumSize(maximumSize: Dimension?) {
        delegate.maximumSize = maximumSize
    }

    override fun isMaximumSizeSet() = delegate.isMaximumSizeSet

    @Deprecated("Deprecated in Java")
    override fun preferredSize() = delegate.preferredSize()

    override fun getPreferredSize() = delegate.preferredSize

    override fun setPreferredSize(preferredSize: Dimension?) {
        delegate.preferredSize = preferredSize
    }

    override fun isPreferredSizeSet() = delegate.isPreferredSizeSet

    @Deprecated("Deprecated in Java")
    override fun resize(d: Dimension?) = delegate.resize(d)

    override fun resize(width: Int, height: Int) = delegate.resize(width, height)
}
