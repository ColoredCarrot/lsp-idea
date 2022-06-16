package info.voidev.lspidea.ui

import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import info.voidev.lspidea.LspIdea
import info.voidev.lspidea.lspex.debug.StackTrace

internal fun ConsoleView.printStackTrace(stackTrace: StackTrace) {
    for (element in stackTrace.elements) {
        print(
            "$element\n",
            if (element.className.startsWith(LspIdea.BASE_PACKAGE_NAME)) {
                ConsoleViewContentType.NORMAL_OUTPUT
            } else {
                ConsoleViewContentType.LOG_VERBOSE_OUTPUT
            }
        )
    }
}
