package info.voidev.lspidea.features.references

import com.intellij.navigation.TargetPresentation
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.pom.Navigatable
import info.voidev.lspidea.util.LspUtils
import info.voidev.lspidea.util.resolveFile
import org.eclipse.lsp4j.Location
import org.eclipse.lsp4j.LocationLink

class LocationWrapper(
    val presentation: TargetPresentation,
    navigable: Navigatable,
    val textForFiltering: String,
) : Navigatable by navigable {
    companion object {
        fun create(project: Project, location: Location): LocationWrapper? {
            val file = location.resolveFile() ?: return null
            val document = FileDocumentManager.getInstance().getDocument(file)

            val locationText = document?.let {
                val line = location.range.start.line
                val text =
                    document.getText(TextRange(document.getLineStartOffset(line), document.getLineEndOffset(line)))
                text.trim()
            }

            return LocationWrapper(
                TargetPresentation
                    .builder(file.presentableName)
                    .locationText(locationText)
                    .presentation(),
                LocationNavigable(project, location),
                file.presentableName + " " + locationText.orEmpty()
            )
        }
    }
}

fun LocationNavigable(project: Project, location: Location) = OpenFileDescriptor(
    project,
    location.resolveFile()!!,
    location.range.start.line,
    location.range.start.character
)

fun LocationLinkNavigable(project: Project, location: LocationLink) = OpenFileDescriptor(
    project,
    LspUtils.resolve(location.targetUri)!!,
    location.targetSelectionRange.start.line,
    location.targetSelectionRange.start.character
)
