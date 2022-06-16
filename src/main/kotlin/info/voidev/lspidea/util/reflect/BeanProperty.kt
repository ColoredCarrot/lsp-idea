package info.voidev.lspidea.util.reflect

import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Type

interface BeanProperty : AnnotatedElement {

    val name: String

    val type: Type

    fun get(instance: Any): Any?

}
