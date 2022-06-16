package info.voidev.lspidea.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@State(
    name = "info.voidev.lspidea.config.LspIdeaConfigState",
)
@Service(Service.Level.PROJECT)
class LspIdeaConfig(project: Project) : PersistentStateComponent<LspIdeaConfig.State> {

    data class State(
        var fooValue: String = "foo",
        var maxSelectionRanges: Int = 25,
    )

    private var state = State()

    override fun getState() = state

    override fun loadState(state: State) {
        this.state = state
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project) = project.service<LspIdeaConfig>()

        @JvmStatic
        fun get(project: Project) = getInstance(project).state
    }
}
