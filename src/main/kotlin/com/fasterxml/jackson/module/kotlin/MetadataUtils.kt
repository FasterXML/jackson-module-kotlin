package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.introspect.Annotated
import kotlinx.metadata.ClassName
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmConstructor
import kotlinx.metadata.KmFunction
import kotlinx.metadata.jvm.JvmMethodSignature
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import kotlinx.metadata.jvm.signature
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Method

internal fun Metadata.toKotlinClassHeader() = KotlinClassHeader(
    this.kind,
    this.metadataVersion,
    this.data1,
    this.data2,
    this.extraString,
    this.packageName,
    this.extraInt
)

internal fun Metadata.toKmClassOrNull(): KmClass? =
    (KotlinClassMetadata.read(this.toKotlinClassHeader()) as? KotlinClassMetadata.Class)?.toKmClass()
internal fun Class<*>.toKmClassOrNull(): KmClass? = this.getAnnotation(Metadata::class.java)?.toKmClassOrNull()
internal fun Annotated.toKmClassOrNull(): KmClass? = this.getAnnotation(Metadata::class.java)?.toKmClassOrNull()

// It may fail when executed for types like `kotlin.Int`.
internal fun ClassName.toJavaClass(): Class<*> {
    return Class.forName(replace(".", "$").replace("/", "."))
}

private val PRIMITIVE_CLASS_TO_DESC = mapOf(
    Byte::class.javaPrimitiveType to 'B',
    Char::class.javaPrimitiveType to 'C',
    Double::class.javaPrimitiveType to 'D',
    Float::class.javaPrimitiveType to 'F',
    Int::class.javaPrimitiveType to 'I',
    Long::class.javaPrimitiveType to 'J',
    Short::class.javaPrimitiveType to 'S',
    Boolean::class.javaPrimitiveType to 'Z',
    Void::class.javaPrimitiveType to 'V'
)

// http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3
private fun Class<*>.toDescriptor(): String = when {
    isPrimitive -> PRIMITIVE_CLASS_TO_DESC.getValue(this).toString()
    isArray -> "[${componentType.toDescriptor()}"
    else -> "L$name;".replace('.', '/')
}

// Contents of the argument JVM Descriptor with the return type removed.
private fun Executable.getParametersJvmDescHead() = parameterTypes
    .joinToString(prefix = "(", separator = "", postfix = ")") { it.toDescriptor() }

// Method to get KmConstructor/KmFunction from Constructor/Method.
// Forced unwrapping because couldn't find an example where signature can't be obtained from a statically defined class.
internal fun KmClass.getKmConstructor(constructor: Constructor<*>): KmConstructor =
    constructors.first { it.signature!!.desc.startsWith(constructor.getParametersJvmDescHead()) }
// If `Method` is a `getter`/`setter` of a Kotlin property, getting it from `functions` will fail,
// so it is defined as `nullable`.
internal fun KmClass.getKmFunction(method: Method): KmFunction? = functions.find {
    val signature: JvmMethodSignature = it.signature!!
    signature.name == this.name && signature.desc.startsWith(method.getParametersJvmDescHead())
}
