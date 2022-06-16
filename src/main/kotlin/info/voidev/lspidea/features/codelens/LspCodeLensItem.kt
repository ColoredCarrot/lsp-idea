package info.voidev.lspidea.features.codelens

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import info.voidev.lspidea.command.LspCommandExecutionUtil
import info.voidev.lspidea.connect.LspSession
import java.awt.event.MouseEvent
import javax.swing.Icon

class LspCodeLensItem(
    session: LspSession,
    codeLens: AutoCodeLens,
    element: PsiElement,
    range: TextRange,
    icon: Icon,
) : LineMarkerInfo<PsiElement>(
    element,
    range,
    icon,
    { codeLens.get().command?.title ?: "unknown" },
    MyNavigationHandler(session, codeLens),
    GutterIconRenderer.Alignment.RIGHT,
    { codeLens.get().command?.title ?: "unknown" },
) {
    private class MyNavigationHandler(
        private val session: LspSession,
        codeLens: AutoCodeLens,
    ) : GutterIconNavigationHandler<PsiElement> {

        private val codeLens by codeLens

        override fun navigate(e: MouseEvent, elt: PsiElement) {
            if (!session.isActive) return
            val command = codeLens.command ?: return

            LspCommandExecutionUtil.execute(command, session)
        }
    }
}
