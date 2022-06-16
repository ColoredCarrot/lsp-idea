package info.voidev.lspidea.toolwindow

import javax.swing.JComponent
import javax.swing.JList
import javax.swing.ListCellRenderer

class CustomizableSelectionAwareListCellRenderer<T>(private val delegate: (T) -> JComponent) : ListCellRenderer<T> {

    override fun getListCellRendererComponent(
        list: JList<out T>,
        value: T,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean,
    ): JComponent {
        val comp = delegate(value)

        comp.isOpaque = true
        if (isSelected) {
            comp.background = list.selectionBackground
            comp.foreground = list.selectionForeground
        } else {
            comp.foreground = list.foreground
        }

        return comp
    }
}
