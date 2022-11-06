package tools.jackson.module.kotlin.test.github

import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializerProvider
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer
import tools.jackson.module.kotlin.jacksonMapperBuilder
import tools.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

// Most of the current behavior has been tested on GitHub464, so only serializer-related behavior is tested here.
class GitHub524 {
    @JvmInline
    value class HasSerializer(val value: Int?)
    class Serializer : StdSerializer<HasSerializer>(HasSerializer::class.java) {
        override fun serialize(value: HasSerializer, gen: JsonGenerator, provider: SerializerProvider) {
            gen.writeString(value.toString())
        }
    }

    @JvmInline
    value class NoSerializer(val value: Int?)

    data class Poko(
        // ULong has a custom serializer defined in Serializers.
        val foo: ULong = ULong.MAX_VALUE,
        // If a custom serializer is set, the ValueClassUnboxSerializer will be overridden.
        val bar: HasSerializer = HasSerializer(1),
        val baz: HasSerializer = HasSerializer(null),
        val qux: HasSerializer? = null,
        // If there is no serializer, it will be unboxed as the existing.
        val quux: NoSerializer = NoSerializer(2)
    )

    @Test
    fun test() {
        val sm = SimpleModule()
            .addSerializer(Serializer())
        val writer = jacksonMapperBuilder().addModule(sm).build().writerWithDefaultPrettyPrinter()

        // 18446744073709551615 is ULong.MAX_VALUE.
        assertEquals(
            """
                {
                  "foo" : 18446744073709551615,
                  "bar" : "HasSerializer(value=1)",
                  "baz" : "HasSerializer(value=null)",
                  "qux" : null,
                  "quux" : 2
                }
            """.trimIndent(),
            writer.writeValueAsString(Poko())
        )
    }

    class SerializeByAnnotation(@get:JsonSerialize(using = Serializer::class) val foo: HasSerializer = HasSerializer(1))

    @Test
    fun failing() {
        val writer = jacksonObjectMapper().writerWithDefaultPrettyPrinter()

        // JsonSerialize is not working now.
        assertNotEquals(
            """
                {
                  "foo" : "HasSerializer(value=1)"
                }
            """.trimIndent(),
            writer.writeValueAsString(SerializeByAnnotation())
        )
    }
}
