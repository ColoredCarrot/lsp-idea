package info.voidev.lspidea.config

import com.intellij.openapi.ui.ComponentContainer

interface Config<S : Any> : ComponentContainer {

    fun apply(): S

    fun reset(state: S)

    fun createDefaults(): S

}
