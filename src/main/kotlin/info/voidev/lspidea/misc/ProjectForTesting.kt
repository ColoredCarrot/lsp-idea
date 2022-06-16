package info.voidev.lspidea.misc

import com.intellij.diagnostic.ActivityCategory
import com.intellij.openapi.extensions.ExtensionsArea
import com.intellij.openapi.extensions.PluginDescriptor
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ExceptionUtilRt
import com.intellij.util.messages.MessageBus
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.SystemIndependent
import org.picocontainer.PicoContainer

@Suppress("UnstableApiUsage")
@Deprecated("delete")
open class ProjectForTesting : UserDataHolderBase(), Project {

    override fun isDisposed(): Boolean {
        return false
    }

    override fun getBasePath(): @SystemIndependent String? {
        return null
    }

    override fun getProjectFile(): VirtualFile? {
        return null
    }

    override fun getName(): String {
        return "Test Project"
    }

    override fun getLocationHash(): String {
        return "dummy"
    }

    override fun getProjectFilePath(): @SystemIndependent String? {
        return null
    }

    override fun getWorkspaceFile(): VirtualFile? {
        return null
    }

    @Deprecated("Deprecated in Java", ReplaceWith("null"))
    override fun getBaseDir(): VirtualFile? {
        return null
    }

    override fun save() {}

    override fun <T> getService(serviceClass: Class<T>): T? {
        return null
    }

    override fun <T> getComponent(interfaceClass: Class<T>): T? {
        return null
    }

    @Deprecated("Deprecated in Java")
    override fun <T> getComponents(baseClass: Class<T>): Array<T> {
        throw UnsupportedOperationException()
    }

    override fun getPicoContainer(): PicoContainer {
        throw UnsupportedOperationException("getPicoContainer is not implement in $javaClass")
    }

    override fun isInjectionForExtensionSupported(): Boolean {
        return false
    }

    override fun getExtensionArea(): ExtensionsArea {
        throw UnsupportedOperationException("getExtensionArea is not implement in $javaClass")
    }

    override fun <T> instantiateClassWithConstructorInjection(
        aClass: Class<T>,
        key: Any,
        pluginId: PluginId,
    ): T {
        throw UnsupportedOperationException()
    }

    override fun getDisposed(): Condition<*> {
        return Condition<Any?> { isDisposed }
    }

    override fun isOpen(): Boolean {
        return false
    }

    override fun isInitialized() = false

    override fun isDefault() = true

    override fun getMessageBus(): MessageBus = throw UnsupportedOperationException()

    override fun dispose() {}

    override fun <T : Any> loadClass(className: String, pluginDescriptor: PluginDescriptor): Class<T> {
        @Suppress("UNCHECKED_CAST")
        return Class.forName(className) as Class<T>
    }

    override fun getActivityCategory(isExtension: Boolean): ActivityCategory {
        return if (isExtension) ActivityCategory.PROJECT_EXTENSION else ActivityCategory.PROJECT_SERVICE
    }

    override fun createError(message: @NonNls String, pluginId: PluginId): RuntimeException {
        return RuntimeException(message)
    }

    override fun createError(
        message: @NonNls String,
        cause: Throwable?,
        pluginId: PluginId,
        attachments: Map<String?, String?>?,
    ): RuntimeException {
        return RuntimeException(message, cause)
    }

    override fun createError(error: Throwable, pluginId: PluginId): RuntimeException {
        ExceptionUtilRt.rethrowUnchecked(error)
        return RuntimeException(error)
    }
}
