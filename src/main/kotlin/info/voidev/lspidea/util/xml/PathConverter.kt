package info.voidev.lspidea.util.xml

import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.util.xmlb.Converter
import java.nio.file.Path
import kotlin.io.path.Path

class PathConverter : Converter<Path>() {

    override fun toString(value: Path) = FileUtilRt.toSystemIndependentName(value.toString())

    override fun fromString(value: String) = Path(FileUtilRt.toSystemDependentName(value))

}
