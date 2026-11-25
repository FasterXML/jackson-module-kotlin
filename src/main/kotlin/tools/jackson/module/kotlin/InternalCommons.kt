package tools.jackson.module.kotlin

import com.fasterxml.jackson.annotation.JsonCreator
import tools.jackson.core.JacksonException
import tools.jackson.databind.util.ClassUtil
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.jvmErasure

internal val defaultConstructorMarker: Class<*> by lazy {
    Class.forName("kotlin.jvm.internal.DefaultConstructorMarker")
}

internal fun JacksonException.wrapWithPath(refFrom: Any?, refFieldName: String) = JacksonException.wrapWithPath(this, refFrom, refFieldName)
internal fun JacksonException.wrapWithPath(refFrom: Any?, index: Int) = JacksonException.wrapWithPath(this, refFrom, index)

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

// Determine if the class is a value class that is unboxed on the JVM, i.e. a Kotlin class annotated with JvmInline.
// KClass.isValue cannot be used for this, because value classes without JvmInline
// (e.g. full value classes since Kotlin 2.5) are compiled as regular classes.
// See https://github.com/FasterXML/jackson-module-kotlin/issues/464
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

internal val ANY_TO_ANY_METHOD_TYPE by lazy {MethodType.methodType(Any::class.java, Any::class.java) }
internal val ANY_TO_INT_METHOD_TYPE by lazy {MethodType.methodType(Int::class.java, Any::class.java) }
internal val ANY_TO_LONG_METHOD_TYPE by lazy {MethodType.methodType(Long::class.java, Any::class.java) }
internal val ANY_TO_STRING_METHOD_TYPE by lazy {MethodType.methodType(String::class.java, Any::class.java) }
internal val ANY_TO_JAVA_UUID_METHOD_TYPE by lazy {MethodType.methodType(UUID::class.java, Any::class.java) }
internal val INT_TO_ANY_METHOD_TYPE by lazy {MethodType.methodType(Any::class.java, Int::class.java) }
internal val LONG_TO_ANY_METHOD_TYPE by lazy {MethodType.methodType(Any::class.java, Long::class.java) }
internal val STRING_TO_ANY_METHOD_TYPE by lazy {MethodType.methodType(Any::class.java, String::class.java) }
internal val JAVA_UUID_TO_ANY_METHOD_TYPE by lazy {MethodType.methodType(Any::class.java, UUID::class.java) }

internal fun unreflectWithAccessibilityModification(method: Method): MethodHandle = MethodHandles.lookup().unreflect(
    method.apply { ClassUtil.checkAndFixAccess(this, false) },
)
internal fun unreflectAsTypeWithAccessibilityModification(
    method: Method,
    type: MethodType,
): MethodHandle = unreflectWithAccessibilityModification(method).asType(type)
