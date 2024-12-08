package tools.jackson.module.kotlin.test.github

import tools.jackson.core.JsonGenerator
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.ObjectWriter
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer
import tools.jackson.module.kotlin.KotlinModule
import tools.jackson.module.kotlin.jacksonMapperBuilder
import tools.jackson.module.kotlin.testPrettyWriter
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Github464 {
    object NullValueClassKeySerializer : StdSerializer<ValueClass>(ValueClass::class.java) {
        override fun serialize(value: ValueClass?, gen: JsonGenerator, provider: SerializationContext) {
            gen.writeName("null-key")
        }
    }

    interface IValue

    @JvmInline
    value class ValueClass(val value: Int?) : IValue
    data class WrapperClass(val inlineField: ValueClass)

    abstract class AbstractGetter<T> {
        abstract val qux: T

        fun <T> getPlugh() = qux
    }
    interface IGetter<T> {
        val quux: T

        fun <T> getXyzzy() = quux
    }

    class Poko(
        val foo: ValueClass,
        val bar: ValueClass?,
        val baz: IValue,
        override val qux: ValueClass,
        override val quux: ValueClass,
        val corge: Collection<ValueClass?>,
        val grault: Array<ValueClass?>,
        val garply: WrapperClass,
        val waldo: WrapperClass?,
        val fred: Map<ValueClass, ValueClass?>
    ) : AbstractGetter<ValueClass>(), IGetter<ValueClass>

    object NullValueSerializer : StdSerializer<Any>(Any::class.java) {
        override fun serialize(value: Any?, gen: JsonGenerator, provider: SerializationContext) {
            gen.writeString("null-value")
        }
    }

    @Nested
    inner class UnboxTest {

        private val zeroValue = ValueClass(0)
        private val oneValue = ValueClass(1)
        private val nullValue = ValueClass(null)

        private val target = Poko(
            foo = zeroValue,
            bar = null,
            baz = zeroValue,
            qux = zeroValue,
            quux = zeroValue,
            corge = listOf(zeroValue, null),
            grault = arrayOf(zeroValue, null),
            garply = WrapperClass(zeroValue),
            waldo = null,
            fred = mapOf(zeroValue to zeroValue, oneValue to null, nullValue to nullValue)
        )

        @Test
        fun test() {
            val writer: ObjectWriter = jacksonMapperBuilder()
                .addModule(
                    SimpleModule()
                        .setDefaultNullKeySerializer(NullValueClassKeySerializer)
                )
                .build()
                .testPrettyWriter()

            assertEquals(
                """
                    {
                      "foo" : 0,
                      "bar" : null,
                      "baz" : 0,
                      "qux" : 0,
                      "quux" : 0,
                      "corge" : [ 0, null ],
                      "grault" : [ 0, null ],
                      "garply" : {
                        "inlineField" : 0
                      },
                      "waldo" : null,
                      "fred" : {
                        "0" : 0,
                        "1" : null,
                        "null-key" : null
                      },
                      "xyzzy" : 0,
                      "plugh" : 0
                    }
                """.trimIndent(),
                writer.writeValueAsString(target)
            )
        }

        @Test
        fun nullValueSerializerTest() {
            val writer = jacksonMapperBuilder()
                .addModule(
                    SimpleModule()
                        .setDefaultNullKeySerializer(NullValueClassKeySerializer)
                        .setDefaultNullValueSerializer(NullValueSerializer)
                )
                .build()
                .testPrettyWriter()

            assertEquals(
                """
                    {
                      "foo" : 0,
                      "bar" : "null-value",
                      "baz" : 0,
                      "qux" : 0,
                      "quux" : 0,
                      "corge" : [ 0, "null-value" ],
                      "grault" : [ 0, "null-value" ],
                      "garply" : {
                        "inlineField" : 0
                      },
                      "waldo" : "null-value",
                      "fred" : {
                        "0" : 0,
                        "1" : "null-value",
                        "null-key" : "null-value"
                      },
                      "xyzzy" : 0,
                      "plugh" : 0
                    }
                """.trimIndent(),
                writer.writeValueAsString(target)
            )
        }
    }

    @JvmInline
    value class ValueBySerializer(val value: Int)

    object Serializer : StdSerializer<ValueBySerializer>(ValueBySerializer::class.java) {
        override fun serialize(value: ValueBySerializer, gen: JsonGenerator, provider: SerializationContext) {
            gen.writeString(value.value.toString())
        }
    }
    object KeySerializer : StdSerializer<ValueBySerializer>(ValueBySerializer::class.java) {
        override fun serialize(value: ValueBySerializer, gen: JsonGenerator, provider: SerializationContext) {
            gen.writeName(value.value.toString())
        }
    }

    @Nested
    inner class SerializerPriorityTest {

        private val target = mapOf(ValueBySerializer(1) to ValueBySerializer(2))
        private val sm = SimpleModule()
            .addSerializer(Serializer)
            .addKeySerializer(ValueBySerializer::class.java, KeySerializer)

        @Test
        fun simpleTest() {
            val om: ObjectMapper = jacksonMapperBuilder().addModule(sm).build()

            assertEquals("""{"1":"2"}""", om.writeValueAsString(target))
        }

        // Currently, there is a situation where the serialization results are different depending on the registration order of the modules.
        // This problem is not addressed because the serializer registered by the user has priority over Extensions.kt,
        // since KotlinModule is basically registered first.
        @Test
        fun priorityTest() {
            val km = KotlinModule.Builder().build()
            val om1: ObjectMapper = JsonMapper.builder().addModules(km, sm).build()
            val om2: ObjectMapper = JsonMapper.builder().addModules(sm, km).build()

            // om1(collect) -> """{"1":"2"}"""
            // om2(broken)  -> """{"1":2}"""
            assertNotEquals(om1.writeValueAsString(target), om2.writeValueAsString(target))
        }
    }
}
