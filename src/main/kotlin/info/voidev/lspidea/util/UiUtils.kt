package info.voidev.lspidea.util

import com.intellij.openapi.util.Disposer
import com.intellij.ui.ListSpeedSearch
import com.intellij.ui.TextAccessor
import com.intellij.ui.components.JBList
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.selected
import com.intellij.ui.layout.ComponentPredicate
import com.intellij.ui.tabs.JBTabs
import com.intellij.ui.tabs.TabInfo
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.toolwindow.CustomizableSelectionAwareListCellRenderer
import javax.swing.AbstractButton
import javax.swing.JComponent
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

fun TabInfo(text: String, component: JComponent) = TabInfo(component).also {
    it.text = text
}

fun JBTabs.addTab(text: String, component: JComponent) = addTab(TabInfo(text, component))

fun <T> JBList<T>.installSpeedSearch(converter: (T) -> String) = ListSpeedSearch(this, converter)

fun <T> JBList<T>.installCellRendererWithBackgroundColors(renderer: (T) -> JComponent) {
    cellRenderer = CustomizableSelectionAwareListCellRenderer(renderer)
}

fun <T : JComponent> Cell<T>.enabledWithSession(session: LspSession) = enabledIf(object : ComponentPredicate() {
    override fun addListener(listener: (Boolean) -> Unit) {
        Disposer.register(session) { listener(false) }
    }

    override fun invoke() = session.isActive
})

fun Cell<AbstractButton>.bindSelectedDirectly(prop: KMutableProperty0<Boolean>) =
    bindSelectedDirectly(prop.getter, prop.setter)

inline fun Cell<AbstractButton>.bindSelectedDirectly(getProp: () -> Boolean, crossinline setProp: (Boolean) -> Unit): Cell<AbstractButton> {
    component.isSelected = getProp()
    selected.addListener { setProp(it) }

    return this
}

operator fun TextAccessor.getValue(thisRef: Any?, property: KProperty<*>): String = text

operator fun TextAccessor.setValue(thisRef: Any?, property: KProperty<*>, value: String) {
    text = value
}
