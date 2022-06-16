package info.voidev.lspidea.features.highlight.ui

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.editor.markup.TextAttributesEffectsBuilder
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComponentContainer
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.ui.CollectionListModel
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.JBColor
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBList
import com.intellij.util.concurrency.Invoker
import info.voidev.lspidea.features.highlight.LspHighlightingMap
import info.voidev.lspidea.features.highlight.LspSemanticTokensListener
import info.voidev.lspidea.features.highlight.LspToken
import javax.swing.JList

class LspSemanticTokensView(private val project: Project) : ComponentContainer, LspSemanticTokensListener {

    private val list = CollectionListModel<LspToken>()
    private val listComp = JBList(list)
    private val edtInvoker = Invoker.forEventDispatchThread(this)

    private val inEditorHighlighters = ArrayList<RangeHighlighter>()

    init {
        listComp.cellRenderer = TokenRenderer()
        listComp.setEmptyText("Semantic tokens unavailable")

        val messageBusConn = project.messageBus.connect()
        Disposer.register(this, messageBusConn)

        messageBusConn.subscribe(LspSemanticTokensListener.TOPIC, this)
        messageBusConn.subscribe(
            ToolWindowManagerListener.TOPIC,
            object : ToolWindowManagerListener {
                override fun stateChanged(toolWindowManager: ToolWindowManager) {
                    val myToolWindow = toolWindowManager.getToolWindow(LspSemanticTokensToolWindowFactory.ID)!!
                    if (myToolWindow.isVisible) {
                        highlightInEditor()
                    } else {
                        clearInEditorHighlights()
                    }
                }

                override fun toolWindowShown(toolWindow: ToolWindow) {
                    if (toolWindow.id == LspSemanticTokensToolWindowFactory.ID) {
                        highlightInEditor()
                    }
                }
            }
        )

        listComp.addListSelectionListener { e ->
            if (!e.valueIsAdjusting) {
                highlightInEditor()
            }
        }
    }

    override fun getComponent() = listComp
    override fun getPreferredFocusableComponent() = listComp

    private class TokenRenderer : ColoredListCellRenderer<LspToken>() {
        override fun customizeCellRenderer(
            list: JList<out LspToken>,
            token: LspToken,
            index: Int,
            selected: Boolean,
            hasFocus: Boolean,
        ) {
            val textAttributesKey = LspHighlightingMap.findStyle(token.type, token.mods)
            val textAttributes = EditorColorsManager.getInstance().globalScheme.getAttributes(textAttributesKey)

            append(token.type, SimpleTextAttributes.fromTextAttributes(textAttributes), true)

            if (token.mods.isNotEmpty()) {
                append(token.mods.joinToString(prefix = " (", separator = ", ", postfix = ")"))
            }
        }
    }

    override fun didUpdate(document: Document, tokens: List<LspToken>?) {
        edtInvoker.invokeLater {
            val editor = FileEditorManager.getInstance(project).selectedTextEditor
            if (editor?.document === document) {
                list.replaceAll(tokens.orEmpty())
            }
        }
    }

    private fun highlightInEditor() {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return
        val markupModel = editor.markupModel

        inEditorHighlighters.forEach(markupModel::removeHighlighter)
        for (token in listComp.selectedValuesList) {
            inEditorHighlighters += markupModel.addRangeHighlighter(
                token.offset,
                token.offset + token.length,
                IN_EDITOR_HIGHLIGHTER_LAYER,
                IN_EDITOR_HIGHLIGHT_TEXT_ATTRS,
                HighlighterTargetArea.EXACT_RANGE
            )
        }

        listComp.selectedValue?.also { token ->
            editor.scrollingModel.scrollTo(editor.offsetToLogicalPosition(token.offset), ScrollType.MAKE_VISIBLE)
        }
    }

    private fun clearInEditorHighlights() {
        FileEditorManager.getInstance(project)
            .selectedTextEditor
            ?.markupModel
            ?.also {
                inEditorHighlighters.forEach(it::removeHighlighter)
            }
        inEditorHighlighters.clear()
    }

    override fun dispose() {
        clearInEditorHighlights()
    }

    companion object {
        private const val IN_EDITOR_HIGHLIGHTER_LAYER = HighlighterLayer.ELEMENT_UNDER_CARET + 50

        private val IN_EDITOR_HIGHLIGHT_TEXT_ATTRS = TextAttributes().apply {
            TextAttributesEffectsBuilder
                .create()
                .coverWith(EffectType.BOXED, JBColor.MAGENTA)
                .applyTo(this)
        }
    }
}
