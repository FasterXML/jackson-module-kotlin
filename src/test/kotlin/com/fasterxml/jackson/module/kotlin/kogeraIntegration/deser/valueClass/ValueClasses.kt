package com.fasterxml.jackson.module.kotlin.kogeraIntegration.deser.valueClass

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.module.kotlin.WrapsNullableValueClassDeserializer

@JvmInline
value class Primitive(val v: Int) {
    class Deserializer : StdDeserializer<Primitive>(Primitive::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Primitive = Primitive(p.intValue + 100)
    }
}

@JvmInline
value class NonNullObject(val v: String) {
    class Deserializer : StdDeserializer<NonNullObject>(NonNullObject::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): NonNullObject =
            NonNullObject(p.valueAsString + "-deser")
    }
}

@JvmInline
value class NullableObject(val v: String?) {
    class DeserializerWrapsNullable : WrapsNullableValueClassDeserializer<NullableObject>(NullableObject::class) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): NullableObject =
            NullableObject(p.valueAsString + "-deser")

        override fun getBoxedNullValue(): NullableObject = NullableObject("null-value-deser")
    }
}
