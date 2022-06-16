package info.voidev.lspidea.debug

import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.jsonrpc.messages.Either3
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class StandardLspDebugRendererProvider : LspDebugRendererProvider() {
    init {
        addValueRenderer<Position> {
            append(it.line)
            appendText(":")
            append(it.character)
        }

        addValueRenderer<Range> {
            append(it.start)
            appendText(" - ")
            append(it.end)
        }

        addValueRenderer<TextDocumentIdentifier> {
            append(it.uri)
        }

        addValueRenderer<Either<*, *>> {
            append(it.left ?: it.right)
        }
        addTypeRenderer<Either<*, *>> {
            if (it !is ParameterizedType) {
                append("union")
            } else {
                val (left, right) = it.actualTypeArguments
                append("(")
                append(left)
                append(" | ")
                append(right)
                append(")")
            }
        }

        addValueRenderer<Either3<*, *, *>> {
            append(it.first ?: it.second ?: it.third)
        }
        addTypeRenderer<Either3<*, *, *>> {
            if (it !is ParameterizedType) {
                append("union")
            } else {
                val (first, second, third) = it.actualTypeArguments
                append("(")
                append(first)
                append(" | ")
                append(second)
                append(" | ")
                append(third)
                append(")")
            }
        }

        // Render native LSP types as unqualified class names
        addTypeRenderer(object : DebugTypeRenderer {
            override val priority get() = -1000

            // Only render if the class is in a trimmable package and
            // there is no further generic information
            override fun canRender(type: Class<*>, genericType: Type?) =
                type.packageName in TRIMMED_PACKAGES && (genericType == null || genericType is Class<*>)

            override fun render(type: Class<*>, genericType: Type?, output: LspDebugTypeRendererOutput) {
                output.append(type.simpleName)
            }

            private val TRIMMED_PACKAGES = hashSetOf(
                "java.lang",
                "java.util",
                Position::class.java.packageName, // Don't hardcode org.eclipse.lsp4j to handle shadowing
            )
        })

        // Render List<T> as T[]
        addTypeRenderer(object : DebugTypeRenderer {
            override fun canRender(type: Class<*>, genericType: Type?) =
                List::class.java.isAssignableFrom(type) && type.packageName == "java.util" && genericType is ParameterizedType

            override fun render(type: Class<*>, genericType: Type?, output: LspDebugTypeRendererOutput) {
                val itemType = (genericType as ParameterizedType).actualTypeArguments[0]
                output.append(itemType)
                output.append("[]")
            }
        })
    }
}
