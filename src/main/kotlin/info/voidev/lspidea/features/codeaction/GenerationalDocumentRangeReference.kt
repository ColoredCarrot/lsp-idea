package info.voidev.lspidea.features.codeaction

import com.intellij.openapi.util.TextRange

data class GenerationalDocumentRangeReference(
    val canonicalDocumentUrl: String,
    val documentModificationStamp: Long,
    val range: TextRange,
)
