package info.voidev.lspidea.config.servers

import com.intellij.util.xmlb.annotations.Tag
import info.voidev.lspidea.def.LspServerSupport
import info.voidev.lspidea.util.xml.Discriminator
import info.voidev.lspidea.util.xml.PolymorphicState

@Tag("options")
class LspServerOptionsConfigState() : PolymorphicState<LspServerOptionsConfigStateInterface>() {

    constructor(value: LspServerOptionsConfigStateInterface) : this() {
        set(value)
    }

    override fun getDiscriminator(value: LspServerOptionsConfigStateInterface): String {
        return value.javaClass.getAnnotation(Discriminator::class.java)!!.value
    }

    override fun getClass(discriminator: String): Class<out LspServerOptionsConfigStateInterface> {
        return LspServerSupport.getById(discriminator)!!.createConfigDefaults().javaClass
    }
}
