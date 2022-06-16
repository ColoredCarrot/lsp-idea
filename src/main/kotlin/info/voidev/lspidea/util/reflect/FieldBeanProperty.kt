package info.voidev.lspidea.util.reflect

import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Field
import java.lang.reflect.Type

class FieldBeanProperty(private val _field: Field) : BeanProperty, AnnotatedElement by _field {

    init {
        _field.isAccessible = true
    }

    override val name: String get() = _field.name

    override val type: Type get() = _field.genericType

    override fun get(instance: Any): Any? = _field.get(instance)
}
