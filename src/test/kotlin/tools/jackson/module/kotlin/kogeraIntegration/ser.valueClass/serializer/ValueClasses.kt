package tools.jackson.module.kotlin.kogeraIntegration.ser.valueClass.serializer

import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ser.std.StdSerializer

@JvmInline
value class Primitive(val v: Int) {
    class Serializer : StdSerializer<Primitive>(Primitive::class.java) {
        override fun serialize(value: Primitive, gen: JsonGenerator, provider: SerializationContext) {
            gen.writeNumber(value.v + 100)
        }
    }
}

@JvmInline
value class NonNullObject(val v: String) {
    class Serializer : StdSerializer<NonNullObject>(NonNullObject::class.java) {
        override fun serialize(value: NonNullObject, gen: JsonGenerator, provider: SerializationContext) {
            gen.writeString("${value.v}-ser")
        }
    }
}

@JvmInline
value class NullableObject(val v: String?) {
    class Serializer : StdSerializer<NullableObject>(NullableObject::class.java) {
        override fun serialize(value: NullableObject, gen: JsonGenerator, provider: SerializationContext) {
            gen.writeString(value.v?.let { "$it-ser" } ?: "NULL")
        }
    }
}
