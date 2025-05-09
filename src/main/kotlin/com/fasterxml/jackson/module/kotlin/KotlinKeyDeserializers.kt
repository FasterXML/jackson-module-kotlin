package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.exc.InputCoercionException
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.std.StdKeyDeserializer
import com.fasterxml.jackson.databind.deser.std.StdKeyDeserializers
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import com.fasterxml.jackson.databind.util.ClassUtil
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaMethod

// The reason why key is treated as nullable is to match the tentative behavior of StdKeyDeserializer.
// If StdKeyDeserializer is modified, need to modify this too.

internal object UByteKeyDeserializer : StdKeyDeserializer(TYPE_SHORT, UByte::class.java) {
    private fun readResolve(): Any = UByteKeyDeserializer

    override fun deserializeKey(key: String?, ctxt: DeserializationContext): UByte? = super.deserializeKey(key, ctxt)
        ?.let {
            (it as Short).asUByte() ?: throw InputCoercionException(
                null,
                "Numeric value (${key}) out of range of UByte (0 - ${UByte.MAX_VALUE}).",
                JsonToken.VALUE_NUMBER_INT,
                UByte::class.java
            )
        }
}

internal object UShortKeyDeserializer : StdKeyDeserializer(TYPE_INT, UShort::class.java) {
    private fun readResolve(): Any = UShortKeyDeserializer

    override fun deserializeKey(key: String?, ctxt: DeserializationContext): UShort? = super.deserializeKey(key, ctxt)
        ?.let {
            (it as Int).asUShort() ?: throw InputCoercionException(
                null,
                "Numeric value (${key}) out of range of UShort (0 - ${UShort.MAX_VALUE}).",
                JsonToken.VALUE_NUMBER_INT,
                UShort::class.java
            )
        }
}

internal object UIntKeyDeserializer : StdKeyDeserializer(TYPE_LONG, UInt::class.java) {
    private fun readResolve(): Any = UIntKeyDeserializer

    override fun deserializeKey(key: String?, ctxt: DeserializationContext): UInt? = super.deserializeKey(key, ctxt)
        ?.let {
            (it as Long).asUInt() ?: throw InputCoercionException(
                null,
                "Numeric value (${key}) out of range of UInt (0 - ${UInt.MAX_VALUE}).",
                JsonToken.VALUE_NUMBER_INT,
                UInt::class.java
            )
        }
}

// kind parameter is dummy.
internal object ULongKeyDeserializer : StdKeyDeserializer(TYPE_LONG, ULong::class.java) {
    private fun readResolve(): Any = ULongKeyDeserializer

    override fun deserializeKey(key: String?, ctxt: DeserializationContext): ULong? = key?.let {
        it.toBigInteger().asULong() ?: throw InputCoercionException(
            null,
            "Numeric value (${key}) out of range of ULong (0 - ${ULong.MAX_VALUE}).",
            JsonToken.VALUE_NUMBER_INT,
            ULong::class.java
        )
    }
}

// The implementation is designed to be compatible with various creators, just in case.
internal class ValueClassKeyDeserializer<S, D : Any>(
    private val creator: Method,
    private val converter: ValueClassBoxConverter<S, D>
) : KeyDeserializer() {
    private val unboxedClass: Class<*> = creator.parameterTypes[0]

    init {
        ClassUtil.checkAndFixAccess(creator, false)
    }

    // Based on databind error
    // https://github.com/FasterXML/jackson-databind/blob/341f8d360a5f10b5e609d6ee0ea023bf597ce98a/src/main/java/com/fasterxml/jackson/databind/deser/DeserializerCache.java#L624
    private fun errorMessage(boxedType: JavaType): String =
        "Could not find (Map) Key deserializer for types wrapped in $boxedType"

    override fun deserializeKey(key: String?, ctxt: DeserializationContext): Any {
        val unboxedJavaType = ctxt.constructType(unboxedClass)

        return try {
            // findKeyDeserializer does not return null, and an exception will be thrown if not found.
            val value = ctxt.findKeyDeserializer(unboxedJavaType, null).deserializeKey(key, ctxt)
            @Suppress("UNCHECKED_CAST")
            converter.convert(creator.invoke(null, value) as S)
        } catch (e: InvalidDefinitionException) {
            throw JsonMappingException.from(ctxt, errorMessage(ctxt.constructType(converter.boxedClass.java)), e)
        }
    }

    companion object {
        fun createOrNull(
            boxedClass: KClass<*>,
            cache: ReflectionCache
        ): ValueClassKeyDeserializer<*, *>? {
            // primaryConstructor.javaMethod for the value class returns constructor-impl
            // Only primary constructor is allowed as creator, regardless of visibility.
            // This is because it is based on the WrapsNullableValueClassBoxDeserializer.
            // Also, as far as I could research, there was no such functionality as JsonKeyCreator,
            // so it was not taken into account.
            val creator = boxedClass.primaryConstructor?.javaMethod ?: return null
            val converter = cache.getValueClassBoxConverter(creator.returnType, boxedClass)

            return ValueClassKeyDeserializer(creator, converter)
        }
    }
}

internal class KotlinKeyDeserializers(private val cache: ReflectionCache) : StdKeyDeserializers() {
    override fun findKeyDeserializer(
        type: JavaType,
        config: DeserializationConfig?,
        beanDesc: BeanDescription?
    ): KeyDeserializer? {
        val rawClass = type.rawClass

        return when {
            rawClass == UByte::class.java -> UByteKeyDeserializer
            rawClass == UShort::class.java -> UShortKeyDeserializer
            rawClass == UInt::class.java -> UIntKeyDeserializer
            rawClass == ULong::class.java -> ULongKeyDeserializer
            rawClass.isUnboxableValueClass() -> ValueClassKeyDeserializer.createOrNull(rawClass.kotlin, cache)
            else -> null
        }
    }
}
