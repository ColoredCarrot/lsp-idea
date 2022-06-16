package info.voidev.lspidea.featurelist

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.extensions.RequiredElement
import com.intellij.util.xmlb.annotations.Attribute
import info.voidev.lspidea.LspIdea

class LspFeatureEP {

    @RequiredElement
    @Attribute
    lateinit var instance: String
        internal set

    fun getFeatureInstance() =
        try {
            Class.forName(instance).getField("INSTANCE").get(null) as LspFeature
        } catch (ex: Exception) {
            logger.error("Invalid $EP_NAME extension", ex)
            null
        }

    companion object {
        @JvmStatic
        val EP_NAME = ExtensionPointName.create<LspFeatureEP>(LspIdea.EP_PREFIX + "feature")

        private val logger = logger<LspFeatureEP>()
    }
}

//class LspFeatureSetEP {
//
//    @Property(surroundWithTag = false)
//    @XCollection
//    lateinit var features: MutableList<FeatureEP>
//        internal set
//
//    @Tag("feature")
//    open class FeatureEP {
//        @RequiredElement
//        @Attribute
//        lateinit var implementation: String
//            internal set
//    }
//
//    companion object {
//        @JvmStatic
//        val EP_NAME = ExtensionPointName.create<LspFeatureSetEP>(LspIdea.EP_PREFIX + "featureSet")
//    }
//}
