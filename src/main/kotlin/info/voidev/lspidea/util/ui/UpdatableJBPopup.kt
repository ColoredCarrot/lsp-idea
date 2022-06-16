package info.voidev.lspidea.util.ui

import com.intellij.openapi.ui.GenericListComponentUpdater
import com.intellij.openapi.ui.popup.JBPopup

class UpdatableJBPopup<T>(
    private val popup: JBPopup,
    private val updater: GenericListComponentUpdater<T>,
) : JBPopup by popup, GenericListComponentUpdater<T> by updater
