package info.voidev.lspidea.config.runner

import com.intellij.collaboration.async.CompletableFutureUtil
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.io.FileUtil
import com.intellij.ui.components.ActionLink
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import info.voidev.lspidea.config.Config
import info.voidev.lspidea.config.servers.LspServerOptionsConfigStateInterface
import info.voidev.lspidea.connect.LspLocalProcessRunner
import info.voidev.lspidea.def.ConfiguredLspServer
import info.voidev.lspidea.def.LspServerSupport
import info.voidev.lspidea.util.getValue
import info.voidev.lspidea.util.setValue
import info.voidev.lspidea.validation.LspServerValidationResult
import info.voidev.lspidea.validation.LspServerValidator
import java.io.File
import javax.swing.JComponent

class LocalProcessLspRunnerConfig(private val origin: LspServerSupport<*>) : Config<LocalProcessLspRunnerConfigState> {

    private val mainComponent: JComponent

    private val executablePath = TextFieldWithBrowseButton()

    var executablePathValue: String by executablePath

    private lateinit var downloadButton: ActionLink

    init {
        executablePath.addBrowseFolderListener(
            "Language Server Executable",
            null,
            null,
            FileChooserDescriptorFactory.createSingleFileDescriptor()
        )

        @Suppress("UnstableApiUsage")
        mainComponent = panel {
            row {
                label("Path to executable:")
                cell(executablePath)
                    .horizontalAlign(HorizontalAlign.FILL)
                /*.also {
                    ComponentValidator(this@LocalProcessLspServerHandlerConfig)
                        .withValidator(Supplier {
                            val path = try {
                                Path.of(executablePathValue)
                            } catch (e: Exception) {
                                return@Supplier null
                            }
                            if (!path.exists()) ValidationInfo("File does not exist", it.component.textField)
                            else if (!path.isExecutable()) ValidationInfo("File is not executable",
                                it.component.textField)
                            else null
                        })
                        .installOn(it.component.textField)
                        .andRegisterOnDocumentListener(it.component.textField)
                }*/
            }
            row {
                if (origin.installer != null) {
                    downloadButton = link("Download...") { downloadExecutable() }.component
                }
                link("Test") { testExecutable() }
            }
        }
    }

    override fun getComponent(): JComponent = mainComponent

    override fun getPreferredFocusableComponent(): JComponent? = executablePath

    override fun apply(): LocalProcessLspRunnerConfigState {
        return LocalProcessLspRunnerConfigState(
            executablePath = executablePathValue,
        )
    }

    override fun reset(state: LocalProcessLspRunnerConfigState) {
        executablePathValue = state.executablePath
    }

    override fun createDefaults() = LocalProcessLspRunnerConfigState()

    override fun dispose() {
    }

    private fun downloadExecutable() {
        origin.installer
            ?.download(downloadButton)
            ?.thenAcceptAsync(
                { path -> executablePathValue = path.toString() },
                CompletableFutureUtil.getEDTExecutor()
            )
    }

    private fun testExecutable() {
        val configState = apply()

        if (!File(configState.executablePath).canExecute()) {
            Messages.showErrorDialog(
                mainComponent,
                "File does not exist or is not executable: ${configState.executablePath}",
                "Missing Language Server"
            )
            return
        }

        // Validate with modal progress indicator
        ProgressManager.getInstance().runProcessWithProgressSynchronously(
            {
                val server = (origin as LspServerSupport<LspServerOptionsConfigStateInterface>).createConfiguredServer(
                    TODO()
                )
                doTestExecutable(server, ProgressManager.getGlobalProgressIndicator()!!)
            },
            "Validating language server",
            true,
            null,
            mainComponent
        )
    }

    private fun doTestExecutable(server: ConfiguredLspServer, progress: ProgressIndicator) {
        val tempDir = FileUtil.createTempDirectory("langserver-test-dir.", null)

        val processHandler = LspLocalProcessRunner(
            ProcessBuilder(File(executablePathValue).absolutePath).directory(tempDir)
        )

        val result = LspServerValidator.validate(server, processHandler, progress)

        FileUtil.delete(tempDir)

        invokeLater {
            if (result is LspServerValidationResult.Success) {
                Messages.showInfoMessage(
                    mainComponent,
                    "${result.serverInfo?.name ?: server.displayName} is valid",
                    "Success"
                )
            } else if (result is LspServerValidationResult.Failure) {
                Messages.showErrorDialog(
                    mainComponent,
                    "The language server failed to complete a basic sample session: ${result.exception}",
                    "Broken Language Server"
                )
            }
        }
    }
}
