package info.voidev.lspidea.debug

import info.voidev.lspidea.util.reflect.wrapperClass

class PrimitiveDebugRendererProvider : LspDebugRendererProvider() {
    init {
        addValueRenderer<String> { appendString(it) }
        addValueRenderer<Boolean> { appendKeyword(if (it) "true" else "false") }

        addValueRenderer(object : LspDebugValueRenderer<Number>(emptyList()) {
            override fun canRender(c: Class<*>): Boolean {
                val cw = c.wrapperClass
                return Number::class.java.isAssignableFrom(cw)
            }

            override fun LspDebugValueRendererOutput.doRender(value: Number) {
                appendNumeric(value.toString())
            }
        })
    }
}
