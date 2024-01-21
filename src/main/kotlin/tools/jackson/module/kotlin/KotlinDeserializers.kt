package tools.jackson.module.kotlin

import tools.jackson.core.JsonParser
import tools.jackson.core.JsonToken.VALUE_NUMBER_INT
import tools.jackson.core.exc.InputCoercionException
import tools.jackson.databind.BeanDescription
import tools.jackson.databind.DeserializationConfig
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JavaType
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.deser.Deserializers
import tools.jackson.databind.deser.std.StdDeserializer
import kotlin.time.Duration as KotlinDuration

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

internal class KotlinDeserializers(
    private val useJavaDurationConversion: Boolean,
) : Deserializers.Base() {
    override fun findBeanDeserializer(
        type: JavaType,
        config: DeserializationConfig?,
        beanDesc: BeanDescription?,
    ): ValueDeserializer<*>? {
        val rawClass = type.rawClass

        return when {
            type.isInterface && rawClass == Sequence::class.java -> SequenceDeserializer
            rawClass == Regex::class.java -> RegexDeserializer
            rawClass == UByte::class.java -> UByteDeserializer
            rawClass == UShort::class.java -> UShortDeserializer
            rawClass == UInt::class.java -> UIntDeserializer
            rawClass == ULong::class.java -> ULongDeserializer
            rawClass == KotlinDuration::class.java ->
                JavaToKotlinDurationConverter.takeIf { useJavaDurationConversion }?.delegatingDeserializer
            else -> null
        }
    }

    override fun hasDeserializerFor(config: DeserializationConfig, valueType: Class<*>): Boolean {
        return valueType == Sequence::class.java
                || valueType == Regex::class.java
                || valueType == UByte::class.java
                || valueType == UShort::class.java
                || valueType == UInt::class.java
                || valueType == ULong::class.java
                || valueType == KotlinDuration::class.java
    }
}
