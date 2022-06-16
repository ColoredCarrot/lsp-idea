package info.voidev.lspidea.features.codelens

import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.util.joinLsp
import org.eclipse.lsp4j.CodeLens
import kotlin.reflect.KProperty

/**
 * A wrapper for [org.eclipse.lsp4j.CodeLens] that auto-resolves.
 */
class AutoCodeLens(codeLens: CodeLens, private val session: LspSession) {

    private var hasAttemptedResolve = false

    var codeLens: CodeLens = codeLens
        get() {
            if (hasAttemptedResolve || field.command != null) return field
            if (!session.isActive) return field

            val resolveResult = session.server.textDocumentService
                .resolveCodeLens(field)
                .joinLsp(session.project, "Could not resolve code lens")

            hasAttemptedResolve = true
            if (resolveResult != null) {
                field = resolveResult
            }

            return field
        }
        private set

    fun get() = codeLens

    operator fun getValue(thisRef: Any?, property: KProperty<*>) = codeLens

}
