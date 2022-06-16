package info.voidev.lspidea.features.references

import com.intellij.codeInsight.hint.HintManager
import com.intellij.collaboration.async.CompletableFutureUtil.handleOnEdt
import com.intellij.find.FindBundle
import com.intellij.navigation.TargetPresentation
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.GenericListComponentUpdater
import com.intellij.pom.Navigatable
import com.intellij.ui.list.buildTargetPopup
import com.intellij.util.concurrency.AppExecutorUtil
import info.voidev.lspidea.LspIdea
import info.voidev.lspidea.LspSessionManager
import info.voidev.lspidea.util.caretLspPosition
import info.voidev.lspidea.util.identifyForLsp
import info.voidev.lspidea.util.ui.UpdatableJBPopup
import org.eclipse.lsp4j.ReferenceContext
import org.eclipse.lsp4j.ReferenceParams
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException
import java.util.concurrent.TimeUnit

class LspReferencesAction : AnAction(), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        if (!handleIfApplicable(e)) {
            ActionManager.getInstance().getAction("ShowUsages")!!.actionPerformed(e)
        }
    }

    private fun handleIfApplicable(e: AnActionEvent): Boolean {
        val project = e.project ?: return false
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return false
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return false
        val session = LspSessionManager.getInstance(project).getForFile(file) ?: return false

        // TODO: Immediately open a popup that says "Loading..."
        //  because otherwise, the user has no feedback anything is happening

        session.server.textDocumentService.references(
            ReferenceParams(
                file.identifyForLsp(),
                editor.caretLspPosition,
                ReferenceContext(false)
            )
        ).handleOnEdt(session) { rawRefs, throwable ->
            if (throwable != null || rawRefs == null) {
                if (throwable is ResponseErrorException) {
                    LspIdea.showResponseError("Could not fetch references", throwable.responseError, project)
                } else {
                    logger<LspReferencesAction>().error("Could not fetch references", throwable)
                }
                return@handleOnEdt
            }

            val refs = rawRefs.mapNotNull { LocationWrapper.create(project, it) }
            showReferences(refs, editor, e.dataContext)
        }

        return true
    }

    // See resolver.kt (findShowUsages)
    private fun showReferences(refs: List<LocationWrapper>?, editor: Editor, dc: DataContext) {
        when {
            refs == null -> {
                val message = FindBundle.message("find.no.usages.at.cursor.error")
                HintManager.getInstance().showErrorHint(editor, message)
            }
            refs.isEmpty() -> {
                val message = FindBundle.message("message.nothingFound")
                HintManager.getInstance().showErrorHint(editor, message)
            }
            refs.size == 1 -> {
                refs.single().navigate(true)
            }
            else -> {
                val popup = buildPopup(refs)
                popup.showInBestPositionFor(dc)
                AppExecutorUtil.getAppScheduledExecutorService().schedule({
                    ApplicationManager.getApplication().invokeLater {
                        val new = LocationWrapper(
                            TargetPresentation.builder("hello").presentation(),
                            object : Navigatable {
                                override fun navigate(requestFocus: Boolean) {
                                }

                                override fun canNavigate(): Boolean {
                                    return false
                                }

                                override fun canNavigateToSource(): Boolean {
                                    return false
                                }
                            },
                            "hello"
                        )
                        popup.replaceModel(refs + new)
                    }
                }, 1000L, TimeUnit.MILLISECONDS)
            }
        }
    }

    private fun buildPopup(refs: List<LocationWrapper>): UpdatableJBPopup<LocationWrapper> {
        val builder = buildTargetPopup(
            refs,
            { it.presentation },
            { it.navigate(true) }
        )
            .setTitle("References")

        @Suppress("UNCHECKED_CAST")
        return UpdatableJBPopup(
            builder.createPopup(),
            // backgroundUpdater is a ListComponentUpdater, which extends GenericListComponentUpdater<PsiElement>
            // (so presumably a *very* old class).
            // We're fine just casting to our use case.
            builder.backgroundUpdater as GenericListComponentUpdater<LocationWrapper>
        )
    }
}
