package info.voidev.lspidea.debug

import info.voidev.lspidea.util.reflect.relatedClass
import java.lang.reflect.Type

class LspDebugTypeRendererOutputImpl : LspDebugTypeRendererOutput {

    private val sb = StringBuilder()

    fun finish() = sb.toString().trim()

    override fun append(type: Type): LspDebugTypeRendererOutputImpl {
        val c = type.relatedClass
        if (c == null) {
            // We cannot continue with our custom type rendering logic
            // and must delegate to Java's
            append(type.typeName)
            return this
        }

        LspDebugRendererProvider.findTypeRenderer(c, type).render(c, type, this)
        return this
    }

    override fun append(text: String): LspDebugTypeRendererOutputImpl {
        sb.append(text)
        return this
    }

}

fun Type.renderForLspDebug() = LspDebugTypeRendererOutputImpl().append(this).finish()
