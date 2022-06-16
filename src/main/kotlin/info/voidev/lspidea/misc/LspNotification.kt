package info.voidev.lspidea.misc

import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager

object LspNotification {

    fun group(): NotificationGroup =
        NotificationGroupManager.getInstance().getNotificationGroup("Language Server")

    fun requestGroup(): NotificationGroup =
        NotificationGroupManager.getInstance().getNotificationGroup("Language Server Request")

}
