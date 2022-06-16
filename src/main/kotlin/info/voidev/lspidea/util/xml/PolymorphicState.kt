package info.voidev.lspidea.util.xml

import com.intellij.configurationStore.deserialize
import com.intellij.util.xmlb.XmlSerializer
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Property
import com.intellij.util.xmlb.annotations.Tag
import org.jdom.Element
import java.util.Objects
import kotlin.reflect.KProperty

@Tag("polymorphic")
abstract class PolymorphicState<T : Any> {

    @Property(surroundWithTag = false)
    private var _element: Element = Element("ERROR")

    @Attribute
    private var discriminator: String = ""

    @com.intellij.util.xmlb.annotations.Transient
    @Transient
    private var cachedValue: T? = null

    protected abstract fun getDiscriminator(value: T): String

    protected abstract fun getClass(discriminator: String): Class<out T>

    @Synchronized
    fun get(): T {
        cachedValue?.also { return it }

        val result = _element.deserialize(getClass(discriminator))
        cachedValue = result
        return result
    }

    @Synchronized
    fun set(value: T) {
        discriminator = getDiscriminator(value)
        _element = XmlSerializer.serialize(value)
        cachedValue = value
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>) = get()

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = set(value)

    @Synchronized
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as PolymorphicState<*>

        return discriminator == other.discriminator && get() == other.get()
    }

    @Synchronized
    override fun hashCode(): Int {
        return Objects.hash(discriminator, get())
    }

}
