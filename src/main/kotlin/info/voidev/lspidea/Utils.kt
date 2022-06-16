package info.voidev.lspidea

import com.intellij.notification.NotificationType
import org.eclipse.lsp4j.MessageType

fun MessageType.asNotificationType() = when (this) {
    MessageType.Error -> NotificationType.ERROR
    MessageType.Warning -> NotificationType.WARNING
    MessageType.Info, MessageType.Log -> NotificationType.INFORMATION
}

inline fun <T> Sequence<T>.sortedBy(crossinline selector: (T) -> UInt) =
    sortedWith(Comparator.comparingInt { selector(it).toInt() })

val Class<*>.wrapperClass
    get() = when (this) {
        java.lang.Boolean.TYPE -> Boolean::class.javaObjectType
        java.lang.Byte.TYPE -> Byte::class.javaObjectType
        java.lang.Character.TYPE -> Char::class.javaObjectType
        java.lang.Short.TYPE -> Short::class.javaObjectType
        java.lang.Integer.TYPE -> Int::class.javaObjectType
        java.lang.Long.TYPE -> Long::class.javaObjectType
        java.lang.Float.TYPE -> Float::class.javaObjectType
        java.lang.Double.TYPE -> Double::class.javaObjectType
        else -> this
    }
