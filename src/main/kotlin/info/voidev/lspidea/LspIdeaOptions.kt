package info.voidev.lspidea

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State

@State(
    name = "LspOptions",
)
class LspIdeaOptions : PersistentStateComponent<LspIdeaOptions.State> {

    private var state = State()

    override fun getState() = state

    override fun loadState(state: State) {
        this.state = state
    }

    data class State(
        var x: String,
    ) {
        constructor() : this("a")
    }

    companion object {
        @JvmStatic
        fun getInstance(): LspIdeaOptions {
            return ApplicationManager.getApplication().getService(LspIdeaOptions::class.java)
        }
    }
}
