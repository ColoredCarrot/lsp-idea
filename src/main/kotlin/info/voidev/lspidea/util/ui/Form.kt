package info.voidev.lspidea.util.ui

import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindSelected
import org.jetbrains.annotations.Nls
import kotlin.reflect.KProperty

@Suppress("UnstableApiUsage")
abstract class Form(private val panel: Panel) {

    protected fun <F : Form> nested(@Nls label: String, form: (Panel) -> F): F {
        lateinit var f: F
        panel.collapsibleGroup(label) {
            f = form(this)
        }.also { it.expanded = true }
        return f
    }

    protected fun checkBox(@Nls label: String): Field<Boolean> {
        val field = Field(false)
        panel.row {
            checkBox(label).bindSelected(field::value)
        }
        return field
    }

    protected fun checkBoxOrDefault(@Nls label: String): Field<Boolean?> {
        val elem = Field<Boolean?>(null)
        panel.buttonGroup {
            row("$label: ") {
                radioButton("Default").bindSelected(
                    { elem.value == null },
                    { if (it) elem.value = null }
                )
                radioButton("Yes").bindSelected(
                    { elem.value == true },
                    { if (it) elem.value = true }
                )
                radioButton("No").bindSelected(
                    { elem.value == false },
                    { if (it) elem.value = false }
                )
            }
        }
        return elem
    }

    protected class Field<T>(var value: T) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>) = value
        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            this.value = value
        }
    }
}
