package info.voidev.lspidea.features.highlight

class LspToken(
    val type: String,
    val mods: List<String>,
    val offset: Int,
    val length: Int,
)
