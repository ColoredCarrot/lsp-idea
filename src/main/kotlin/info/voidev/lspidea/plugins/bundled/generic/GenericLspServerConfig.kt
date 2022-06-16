package info.voidev.lspidea.plugins.bundled.generic

import com.intellij.ide.BrowserUtil
import com.intellij.ui.ContextHelpLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import info.voidev.lspidea.config.Config
import info.voidev.lspidea.util.PathPattern
import javax.swing.JComponent

class GenericLspServerConfig : Config<GenericLspServerConfigState> {

    private val mainComponent: JComponent

    private val filenameRegexTextField = JBTextField()

    init {
        @Suppress("UnstableApiUsage")
        mainComponent = panel {
            row {
                cell(
                    ContextHelpLabel.createWithLink(
                        null,
                        //language=HTML
                        """
                        <strong>Examples:</strong>
                        <ul>
                          <li><code>**.{java,kotlin}</code></li>
                          <li><code>regex:.*\.(java|kt)</code></li>
                        </ul>
                        <p>
                        The syntax is that of Java's PathMatcher,
                        except that 'glob:' is used if no syntax specifier is present.
                        <p>
                        You may need to <strong>restart the IDE</strong> for changes to take effect.
                        """.trimIndent(),
                        "Syntax specification",
                        true
                    ) { BrowserUtil.browse("https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html#getPathMatcher-java.lang.String-") }
                        .also { it.text = "Filename pattern:" }
                )

                cell(filenameRegexTextField)
                    .horizontalAlign(HorizontalAlign.FILL)
            }
        }
    }

    override fun getComponent() = mainComponent

    override fun getPreferredFocusableComponent() = null

    override fun apply() = GenericLspServerConfigState(
        filenamePattern = PathPattern(filenameRegexTextField.text),
    )

    override fun reset(state: GenericLspServerConfigState) {
        filenameRegexTextField.text = state.filenamePattern?.pattern ?: ""
    }

    override fun createDefaults() = GenericLspServerConfigState()

    override fun dispose() {
    }

}
