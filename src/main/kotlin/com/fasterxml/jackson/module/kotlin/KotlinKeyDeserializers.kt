package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.exc.InputCoercionException
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.std.StdKeyDeserializer
import com.fasterxml.jackson.databind.deser.std.StdKeyDeserializers

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

internal object KotlinKeyDeserializers : StdKeyDeserializers() {
    private fun readResolve(): Any = KotlinKeyDeserializers

    override fun findKeyDeserializer(
        type: JavaType,
        config: DeserializationConfig?,
        beanDesc: BeanDescription?
    ): KeyDeserializer? = when (type.rawClass) {
        UByte::class.java -> UByteKeyDeserializer
        UShort::class.java -> UShortKeyDeserializer
        UInt::class.java -> UIntKeyDeserializer
        ULong::class.java -> ULongKeyDeserializer
        else -> null
    }
}
