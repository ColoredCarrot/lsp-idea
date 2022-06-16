package info.voidev.lspidea.featurelist

/*
Idea:
A features tool window, feature-specific settings
 */
interface LspFeatureRegistry {

    fun register(feature: LspFeature)

    fun unregister(feature: LspFeature)
}
