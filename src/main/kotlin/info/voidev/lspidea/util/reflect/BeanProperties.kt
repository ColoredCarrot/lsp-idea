package info.voidev.lspidea.util.reflect

import java.lang.reflect.Field
import java.lang.reflect.Modifier

// TODO maybe consider XmlSerializer.getBeanBinding

object BeanProperties : ClassValue<Collection<BeanProperty>>() {

    override fun computeValue(type: Class<*>): Collection<BeanProperty> {
        if (type.isArray || type.isPrimitive || type.isEnum || type.isInterface) {
            return emptyList()
        }

        return getAllFields(type)
            .filter { !Modifier.isStatic(it.modifiers) && !Modifier.isTransient(it.modifiers) }
            .map { FieldBeanProperty(it) }
            .toList()
    }

    private fun getAllFields(type: Class<*>): Sequence<Field> =
        generateSequence(type, Class<*>::getSuperclass)
            .flatMap { it.declaredFields.asSequence() }
}
