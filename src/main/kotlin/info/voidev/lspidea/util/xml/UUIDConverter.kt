package info.voidev.lspidea.util.xml

import com.intellij.util.xmlb.Converter
import java.util.UUID

class UUIDConverter : Converter<UUID>() {

    override fun toString(value: UUID) = value.toString()

    override fun fromString(value: String): UUID = UUID.fromString(value)

}
