package info.voidev.lspidea.util.xml

import com.intellij.util.xmlb.Converter
import info.voidev.lspidea.util.PathPattern

class PathPatternConverter : Converter<PathPattern?>() {

    override fun toString(value: PathPattern) = value.pattern

    override fun fromString(value: String): PathPattern? {
        if (value.isBlank()) {
            return null
        }
        return PathPattern(value.trim())
    }

}
