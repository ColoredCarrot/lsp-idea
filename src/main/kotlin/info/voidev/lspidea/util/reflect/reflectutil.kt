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
