package info.voidev.lspidea.util.xml

import com.intellij.util.xmlb.Converter

class RegexConverter : Converter<Regex?>() {

    override fun toString(value: Regex) = value.pattern

    override fun fromString(value: String): Regex? {
        if (value.isBlank()) {
            return null
        }
        return Regex(value.trim(), RegexOption.IGNORE_CASE)
    }
}
