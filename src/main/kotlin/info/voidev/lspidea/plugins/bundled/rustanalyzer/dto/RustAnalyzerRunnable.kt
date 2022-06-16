package info.voidev.lspidea.plugins.bundled.rustanalyzer.dto

import org.eclipse.lsp4j.LocationLink

/** DTO from rust-analyzer */
open class RustAnalyzerRunnable {

    lateinit var label: String

    var location: LocationLink? = null

    /** @see RustAnalyzerRunnableKind */
    lateinit var kind: String

    lateinit var args: CargoRunnable
}
