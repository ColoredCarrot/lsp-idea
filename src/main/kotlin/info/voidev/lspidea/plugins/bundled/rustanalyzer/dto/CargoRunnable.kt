package info.voidev.lspidea.plugins.bundled.rustanalyzer.dto

/** DTO from rust-analyzer */
open class CargoRunnable {

    /**
     * command to be executed instead of cargo
     */
    var overrideCargo: String? = null

    var workspaceRoot: String? = null

    /**
     * command, --package and --lib stuff
     */
    lateinit var cargoArgs: MutableList<String>

    /**
     * user-specified additional cargo args, like `--release`.
     */
    lateinit var cargoExtraArgs: MutableList<String>

    /**
     * stuff after --
     */
    lateinit var executableArgs: MutableList<String>

    var expectTest: Boolean? = null
}
