package info.voidev.lspidea.plugins.bundled.rustanalyzer.config

import com.intellij.util.xmlb.annotations.Property
import com.intellij.util.xmlb.annotations.Tag

/**
 * DTO passed to rust-analyzer as well as
 * persistence object.
 *
 * Might get split into two classes if that becomes necessary.
 */
@Tag("initOptions")
data class RustAnalyzerInitOptions(
    @Property(surroundWithTag = false)
    var cargo: Cargo? = null,

    @Property(surroundWithTag = false)
    var procMacro: ProcMacro? = null,

    @Property(surroundWithTag = false)
    var hover: Hover? = null,

    @Property(surroundWithTag = false)
    var hoverActions: HoverActions? = null,

    @Property(surroundWithTag = false)
    var lens: Lens? = null,
) {

    @Tag("cargo")
    data class Cargo(
        @Tag
        var allFeatures: Boolean? = null,
    )

    @Tag("procMacro")
    data class ProcMacro(
        @Tag
        var enable: Boolean? = null,
    )

    @Tag("hover")
    data class Hover(
        @Tag
        var documentation: Boolean? = null,
        @Tag
        var linksInHover: Boolean? = null,
    )

    @Tag("hoverActions")
    data class HoverActions(
        /**
         * Whether to show Debug action. Only applies when `rust-analyzer.hoverActions.enable` is set.
         */
        @Tag
        var debug: Boolean? = false, // we don't support debugging yet, so let's hardcode this to false
    )

    @Tag("lens")
    data class Lens(
        /**
         * Whether to show Method References lens. Only applies when `rust-analyzer.lens.enable` is set.
         */
        @Tag
        var methodReferences: Boolean? = null,
    )
}
