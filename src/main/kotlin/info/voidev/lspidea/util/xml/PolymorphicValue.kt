package info.voidev.lspidea.util.xml

import com.intellij.configurationStore.deserialize
import com.intellij.util.xmlb.XmlSerializer
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Property
import com.intellij.util.xmlb.annotations.Tag
import org.jdom.Element
import kotlin.reflect.KProperty

@Deprecated("use PolymorphicState instead")
@Tag("polymorphic")
class PolymorphicValue<T : Any>(
    @Property(surroundWithTag = false) @Tag("element") var _element: Element? = null
) {

    @com.intellij.util.xmlb.annotations.Transient
    @Transient
    private var cachedValue: T? = null

    @Attribute
    private var discriminator: String? = null

    @get:com.intellij.util.xmlb.annotations.Transient
    @field:com.intellij.util.xmlb.annotations.Transient
    @field:Transient
    lateinit var allowedClasses: Collection<Class<out T>>

    fun get(): T? {
        if (cachedValue != null) {
            return cachedValue
        }

        val elem = _element ?: return null
        val discriminator = elem.getAttributeValue("discriminator") ?: return null
        val theClass = allowedClasses.firstOrNull { it.name == discriminator } ?: return null

        val result = elem.deserialize(theClass)
        cachedValue = result
        return result
    }

    fun set(value: T?) {
        cachedValue = value

        if (value == null) {
            _element = null
            discriminator = null
            return
        }

        require(value.javaClass in allowedClasses)

        _element = XmlSerializer.serialize(value)
        discriminator = value.javaClass.name
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>) = get()

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) = set(value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PolymorphicValue<*>) return false

        return get() == other.get()
    }

    override fun hashCode(): Int {
        return get().hashCode()
    }

}
