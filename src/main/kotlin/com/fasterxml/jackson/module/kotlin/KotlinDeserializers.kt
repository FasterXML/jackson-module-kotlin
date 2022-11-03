package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken.VALUE_NUMBER_INT
import com.fasterxml.jackson.core.exc.InputCoercionException
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

object SequenceDeserializer : StdDeserializer<Sequence<*>>(Sequence::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Sequence<*> {
        return ctxt.readValue(p, List::class.java).asSequence()
    }
}

object RegexDeserializer : StdDeserializer<Regex>(Regex::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Regex {
        val node = ctxt.readTree(p)

        if (node.isTextual) {
            return Regex(node.asText())
        } else if (node.isObject) {
            val pattern = node.get("pattern").asText()
            val options = if (node.has("options")) {
                val optionsNode = node.get("options")
                if (!optionsNode.isArray) {
                    throw IllegalStateException("Expected an array of strings for RegexOptions, but type was ${node.nodeType}")
                }
                optionsNode.elements().asSequence().map { RegexOption.valueOf(it.asText()) }.toSet()
            } else {
                emptySet()
            }
            return Regex(pattern, options)
        } else {
            throw IllegalStateException("Expected a string or an object to deserialize a Regex, but type was ${node.nodeType}")
        }
    }
}

object UByteDeserializer : StdDeserializer<UByte>(UByte::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext) =
        p.shortValue.asUByte() ?: throw InputCoercionException(
            p,
            "Numeric value (${p.text}) out of range of UByte (0 - ${UByte.MAX_VALUE}).",
            VALUE_NUMBER_INT,
            UByte::class.java
        )
}

object UShortDeserializer : StdDeserializer<UShort>(UShort::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext) =
        p.intValue.asUShort() ?: throw InputCoercionException(
            p,
            "Numeric value (${p.text}) out of range of UShort (0 - ${UShort.MAX_VALUE}).",
            VALUE_NUMBER_INT,
            UShort::class.java
        )
}

object UIntDeserializer : StdDeserializer<UInt>(UInt::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext) =
        p.longValue.asUInt() ?: throw InputCoercionException(
            p,
            "Numeric value (${p.text}) out of range of UInt (0 - ${UInt.MAX_VALUE}).",
            VALUE_NUMBER_INT,
            UInt::class.java
        )
}

object ULongDeserializer : StdDeserializer<ULong>(ULong::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext) =
        p.bigIntegerValue.asULong() ?: throw InputCoercionException(
            p,
            "Numeric value (${p.text}) out of range of ULong (0 - ${ULong.MAX_VALUE}).",
            VALUE_NUMBER_INT,
            ULong::class.java
        )
}

internal class KotlinDeserializers : Deserializers.Base() {
    override fun findBeanDeserializer(
        type: JavaType,
        config: DeserializationConfig?,
        beanDesc: BeanDescription?
    ): JsonDeserializer<*>? {
        return when {
            type.isInterface && type.rawClass == Sequence::class.java -> SequenceDeserializer
            type.rawClass == Regex::class.java -> RegexDeserializer
            type.rawClass == UByte::class.java -> UByteDeserializer
            type.rawClass == UShort::class.java -> UShortDeserializer
            type.rawClass == UInt::class.java -> UIntDeserializer
            type.rawClass == ULong::class.java -> ULongDeserializer
            else -> null
        }
    }
}
