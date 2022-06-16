package info.voidev.lspidea.config.serverhandler

import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.xmlb.Converter
import com.intellij.util.xmlb.XmlSerializer
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Property
import com.intellij.util.xmlb.annotations.Tag
import org.jdom.Element

/**
 * Contains the server runner type as well as any configuration state
 * specific to that runner type.
 */
@Tag("runner")
class LspRunnerConfigState internal constructor() {

    @Attribute(converter = LspServerHandlerTypeConverter::class)
    var type: LspRunnerType<*>? = null
        private set

    @Property(surroundWithTag = false)
    @Tag("config")
    private var serializedConfig: Element? = null

    @Transient
    private var cachedConfig: LspRunnerConfigStateInterface? = null

    constructor(type: LspRunnerType<*>?, config: LspRunnerConfigStateInterface?) : this() {
        set(type, config)
    }

    @Synchronized
    fun getConfig(): LspRunnerConfigStateInterface? {
        if (type == null) {
            return null
        }

        if (cachedConfig == null) {
            cachedConfig = XmlSerializer.deserialize(serializedConfig!!, type!!.stateClass)
        }
        return cachedConfig!!
    }

    @Synchronized
    fun set(type: LspRunnerType<*>?, config: LspRunnerConfigStateInterface?) {
        ApplicationManager.getApplication().assertIsDispatchThread()

        require((type == null) == (config == null))
        require(config?.javaClass == type?.stateClass)

        this.type = type
        cachedConfig = config
        serializedConfig = config?.let(XmlSerializer::serialize)
    }

    @Synchronized
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LspRunnerConfigState) return false

        ApplicationManager.getApplication().assertIsDispatchThread()

        return type == other.type && getConfig() == other.getConfig()
    }

    @Synchronized
    override fun hashCode(): Int {
        ApplicationManager.getApplication().assertIsDispatchThread()

        var result = type.hashCode()
        result = 31 * result + getConfig().hashCode()
        return result
    }

}

class LspServerHandlerTypeConverter : Converter<LspRunnerType<*>>() {
    override fun toString(value: LspRunnerType<*>) = value.id
    override fun fromString(value: String) = LspRunnerType.get(value)
}
