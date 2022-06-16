package info.voidev.lspidea.debug

import java.lang.reflect.Type

interface LspDebugTypeRendererOutput {

    fun append(type: Type): LspDebugTypeRendererOutput

    fun append(text: String): LspDebugTypeRendererOutput

}
