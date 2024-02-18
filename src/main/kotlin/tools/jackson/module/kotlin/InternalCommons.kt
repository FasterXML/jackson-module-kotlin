package tools.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonCreator
import tools.jackson.databind.DatabindException
import java.lang.reflect.AnnotatedElement
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.jvmErasure

internal val defaultConstructorMarker: Class<*> by lazy {
    Class.forName("kotlin.jvm.internal.DefaultConstructorMarker")
}

internal fun DatabindException.wrapWithPath(refFrom: Any?, refFieldName: String) = DatabindException.wrapWithPath(this, refFrom, refFieldName)
internal fun DatabindException.wrapWithPath(refFrom: Any?, index: Int) = DatabindException.wrapWithPath(this, refFrom, index)

internal fun Int.toBitSet(): BitSet {
    var i = this
    var index = 0
    val bits = BitSet(32)
    while (i != 0) {
        if (i % 2 != 0) {
            bits.set(index)
        }
        ++index
        i = i shr 1
    }
    return bits
}

// In the future, value classes without @JvmInline will be available, and unboxing may not be able to handle it.
// https://github.com/FasterXML/jackson-module-kotlin/issues/464
// The JvmInline annotation can be added to Java classes,
// so the isKotlinClass decision is necessary (the order is preferable in terms of possible frequency).
internal fun Class<*>.isUnboxableValueClass() = annotations.any { it is JvmInline } && this.isKotlinClass()

internal fun KType.erasedType(): Class<out Any> = this.jvmErasure.java

internal fun AnnotatedElement.hasCreatorAnnotation(): Boolean = getAnnotation(JsonCreator::class.java)
    ?.let { it.mode != JsonCreator.Mode.DISABLED }
    ?: false

// Determine if the unbox result of value class is nullable
internal fun KClass<*>.wrapsNullable(): Boolean =
    this.memberProperties.first { it.javaField != null }.returnType.isMarkedNullable
