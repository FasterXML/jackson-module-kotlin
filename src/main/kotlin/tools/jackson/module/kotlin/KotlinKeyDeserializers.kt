package tools.jackson.module.kotlin

import tools.jackson.core.JsonToken
import tools.jackson.core.exc.InputCoercionException
import tools.jackson.databind.*
import tools.jackson.databind.deser.jdk.JDKKeyDeserializer
import tools.jackson.databind.deser.jdk.JDKKeyDeserializers

// The reason why key is treated as nullable is to match the tentative behavior of JDKKeyDeserializer.
// If JDKKeyDeserializer is modified, need to modify this too.

internal object UByteKeyDeserializer : JDKKeyDeserializer(TYPE_SHORT, UByte::class.java) {
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

internal object UShortKeyDeserializer : JDKKeyDeserializer(TYPE_INT, UShort::class.java) {
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

internal object UIntKeyDeserializer : JDKKeyDeserializer(TYPE_LONG, UInt::class.java) {
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
internal object ULongKeyDeserializer : JDKKeyDeserializer(TYPE_LONG, ULong::class.java) {
    override fun deserializeKey(key: String?, ctxt: DeserializationContext): ULong? = key?.let {
        it.toBigInteger().asULong() ?: throw InputCoercionException(
            null,
            "Numeric value (${key}) out of range of ULong (0 - ${ULong.MAX_VALUE}).",
            JsonToken.VALUE_NUMBER_INT,
            ULong::class.java
        )
    }
}

internal object KotlinKeyDeserializers : JDKKeyDeserializers() {
    override fun findKeyDeserializer(
        type: JavaType,
        config: DeserializationConfig?,
        beanDesc: BeanDescription?,
    ): KeyDeserializer? = when (type.rawClass) {
        UByte::class.java -> UByteKeyDeserializer
        UShort::class.java -> UShortKeyDeserializer
        UInt::class.java -> UIntKeyDeserializer
        ULong::class.java -> ULongKeyDeserializer
        else -> null
    }
}
