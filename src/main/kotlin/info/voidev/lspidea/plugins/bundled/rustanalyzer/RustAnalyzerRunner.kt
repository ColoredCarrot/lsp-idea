package info.voidev.lspidea.plugins.bundled.rustanalyzer

import com.intellij.execution.RunContentExecutor
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.filters.PatternBasedFileHyperlinkFilter
import com.intellij.execution.filters.PatternBasedFileHyperlinkRawDataFinder
import com.intellij.execution.filters.PatternHyperlinkFormat
import com.intellij.execution.filters.PatternHyperlinkPart
import com.intellij.execution.filters.UrlFilter
import com.intellij.execution.process.KillableColoredProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.Disposer
import info.voidev.lspidea.connect.LspSession
import info.voidev.lspidea.plugins.bundled.rustanalyzer.dto.CargoRunnable
import java.util.regex.Pattern

/**
 * Support for executing [CargoRunnable]s.
 */
class RustAnalyzerRunner(private val session: LspSession) {

    private val urlFilter = UrlFilter(session.project)

    fun run(runnable: CargoRunnable, label: String) {

        // Note: check out
        // - https://plugins.jetbrains.com/docs/intellij/run-configuration-execution.html#displaying-process-output
        // - AnalyzeStacktraceUtil.addConsole()

        // Step 1: Construct command line (OS-independent arguments, working directory)
        val commandLine = GeneralCommandLine(
            runnable.overrideCargo ?: "cargo",
            *runnable.cargoArgs.toTypedArray(),
            *runnable.cargoExtraArgs.toTypedArray(),
            "--",
            *runnable.executableArgs.toTypedArray(),
        )
            .withWorkDirectory(runnable.workspaceRoot)
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)

        // Step 2: Create a process handler (not yet started!)
        val process = KillableColoredProcessHandler(commandLine)

        // Step 3: To display the "Process terminated with exit code X" message, attach this listener
        ProcessTerminatedListener.attach(process, session.project)

        // Step 4: The RunContentExecutor takes care of
        //  - creating the ConsoleView (attached to the process) and adding it to RunContentManager
        //  - providing stop/rerun actions
        val executor = RunContentExecutor(session.project, process)
            // Displayed as title of the Run tab
            .withTitle(label)
            // Filters for e.g. making URLs in the output clickable
            .withFilter(urlFilter)
            .withFilter(createRustCompilerFileLinksFilter(runnable))
            // Curiously, the stop action isn't provided by default
            .withStop(
                { process.killProcess() },
                { process.isStartNotified && !process.isProcessTerminated }
            )
            .withRerun { run(runnable, label) }

        Disposer.register(session, executor)
        executor.run()
    }

    /**
     * A filter to inject URLs into file references in Rust's compiler output,
     * e.g. `warning: something  --> main.rs:11:3`
     */
    private fun createRustCompilerFileLinksFilter(runnable: CargoRunnable) = PatternBasedFileHyperlinkFilter(
        session.project,
        runnable.workspaceRoot ?: session.project.guessProjectDir()?.canonicalPath,
        PatternBasedFileHyperlinkRawDataFinder(arrayOf(PatternHyperlinkFormat(
            Pattern.compile("--> (.+\\.rs):(\\d+):(\\d+)", Pattern.CASE_INSENSITIVE),
            false,
            false,
            PatternHyperlinkPart.PATH, PatternHyperlinkPart.LINE, PatternHyperlinkPart.COLUMN
        )))
    )

}
