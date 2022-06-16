package info.voidev.lspidea.features.highlight

import com.intellij.openapi.editor.Document
import com.intellij.util.messages.Topic

interface LspSemanticTokensListener {

    fun didUpdate(document: Document, tokens: List<LspToken>?)

    companion object {
        @Topic.ProjectLevel
        @JvmStatic
        val TOPIC = Topic.create("LSP semantic tokens notifications", LspSemanticTokensListener::class.java)
    }
}
