package info.voidev.lspidea.lspex.inlay

enum class InlayHintKind(val value: Int) {
    Type(1),
    Parameter(2),
    ;

    companion object {
        @JvmStatic
        fun forValue(value: Int) = values()[value - 1].also { assert(it.value == value) }
    }
}
