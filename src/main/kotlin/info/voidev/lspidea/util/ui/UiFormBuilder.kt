package info.voidev.lspidea.util.ui

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected
import info.voidev.lspidea.util.bindSelectedDirectly
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

@Suppress("UnstableApiUsage")
class UiFormBuilder<T : Any>(private val live: T, private val cache: T, private val cache2: T, private val klass: KClass<T>) {

    init {
        require(live !== cache) { "live and cache must be referentially distinct objects" }
    }

    /**
     * Constructs a form whose live state is bound to [live].
     *
     * [cache] contains defaults and may also be updated at times.
     */
    fun build(): DialogPanel {
        return panel {
            buildInto()
        }
    }

    fun Panel.buildInto() {
        for (prop in klass.memberProperties) {
            if (prop !is KMutableProperty1) {
                logger.error("Non-mutable property $prop cannot be bound to a form")
                continue
            }

            property(prop)
        }
    }

    private fun Panel.property(prop: KMutableProperty1<T, *>) {
        when (prop.returnType.classifier as KClass<*>) {
            Boolean::class -> {
                if (prop.returnType.isMarkedNullable) {
                    optionalBoolean(prop as KMutableProperty1<T, Boolean?>)
                } else {
                    TODO()
                }
            }
            else -> {
                // A nested object
                collapsibleGroup(prop.label) {
                    val subLive = prop.get(cache)!!
                    val subCache = prop.get(cache2)!!
                    UiFormBuilder(subLive, subCache, subCache, subLive.javaClass.kotlin)
                        .apply { buildInto() }
                }
            }
//            else -> logger.error("Cannot create form for property ${prop.name} of type ${prop.returnType}")
        }
    }

    private fun Panel.optionalBoolean(prop: KMutableProperty1<T, Boolean?>) = buttonGroup {
        row {
            radioButton("Default")
                .bindSelectedDirectly(
                    { prop.get(live) == null },
                    {
                        if (it) prop.set(live, null)
                    }
                )
            val customValueBtn = radioButton("Set:")
                .bindSelectedDirectly(
                    { prop.get(live) != null },
                    {
                        if (it) prop.set(live, prop.get(cache) ?: false)
                    }
                )

            checkBox(prop.label)
                .bindSelectedDirectly(
                    { prop.get(live) ?: prop.get(cache) ?: false },
                    { prop.set(live, it); prop.set(cache, it) }
                )
                .enabledIf(customValueBtn.selected)
        }
    }

    private val KProperty<*>.label: String
        get() {
            findAnnotation<FormName>()?.value?.also { return it }

            val name = name
            val sb = StringBuilder(name.length + 4)

            var isFirstChar = true
            for (c in name) {
                val isNewWord = c.isUpperCase() || isFirstChar

                if (isNewWord && !isFirstChar) {
                    sb.append(' ')
                }

                sb.append(if (isFirstChar) c.uppercaseChar() else c.lowercaseChar())

                isFirstChar = false
            }

            return sb.toString()
        }

    companion object {
        private val logger = logger<UiFormBuilder<*>>()
    }
}
