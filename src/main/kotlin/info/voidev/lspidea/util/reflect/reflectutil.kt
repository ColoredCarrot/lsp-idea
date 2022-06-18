package info.voidev.lspidea.util.reflect

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.jr.type.ResolvedType
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

val Type.relatedClass: Class<*>?
    get() = when (this) {
        // Standard Java types
        is Class<*> -> this
        is ParameterizedType -> rawType.relatedClass
        is GenericArrayType -> genericComponentType.relatedClass?.arrayTypeJava11()

        // Jackson types
        is ResolvedType -> erasedType()
        is JavaType -> rawClass

        // Unknown
        else -> null
    }

/**
 * Polyfill for [Class.arrayType] for Java versions before Java 12.
 */
fun Class<*>.arrayTypeJava11() = java.lang.reflect.Array.newInstance(this).javaClass
