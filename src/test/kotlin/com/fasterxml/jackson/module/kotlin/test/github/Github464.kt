package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals

class Github464 {
    class UnboxTest {
        object NullValueClassKeySerializer : StdSerializer<ValueClass>(ValueClass::class.java) {
            override fun serialize(value: ValueClass?, gen: JsonGenerator, provider: SerializerProvider) {
                gen.writeFieldName("null-key")
            }
        }

        @JvmInline
        value class ValueClass(val value: Int?)
        data class WrapperClass(val inlineField: ValueClass)

        class Poko(
            val foo: ValueClass,
            val bar: ValueClass?,
            @JvmField
            val baz: ValueClass,
            val qux: Collection<ValueClass?>,
            val quux: Array<ValueClass?>,
            val corge: WrapperClass,
            val grault: WrapperClass?,
            val garply: Map<ValueClass, ValueClass?>
        )

        private val zeroValue = ValueClass(0)
        private val oneValue = ValueClass(1)
        private val nullValue = ValueClass(null)

        private val target = Poko(
            foo = zeroValue,
            bar = null,
            baz = zeroValue,
            qux = listOf(zeroValue, null),
            quux = arrayOf(zeroValue, null),
            corge = WrapperClass(zeroValue),
            grault = null,
            garply = mapOf(zeroValue to zeroValue, oneValue to null, nullValue to nullValue)
        )

        @Test
        fun test() {
            @Suppress("UNCHECKED_CAST")
            val writer: ObjectWriter = jacksonObjectMapper()
                .apply { serializerProvider.setNullKeySerializer(NullValueClassKeySerializer as JsonSerializer<Any?>) }
                .writerWithDefaultPrettyPrinter()

            assertEquals(
                """
                    {
                      "foo" : 0,
                      "bar" : null,
                      "baz" : 0,
                      "qux" : [ 0, null ],
                      "quux" : [ 0, null ],
                      "corge" : {
                        "inlineField" : 0
                      },
                      "grault" : null,
                      "garply" : {
                        "0" : 0,
                        "1" : null,
                        "null-key" : null
                      }
                    }
                """.trimIndent(),
                writer.writeValueAsString(target)
            )
        }

        object NullValueSerializer : StdSerializer<Any>(Any::class.java) {
            override fun serialize(value: Any?, gen: JsonGenerator, provider: SerializerProvider) {
                gen.writeString("null-value")
            }
        }

        @Test
        fun nullValueSerializerTest() {
            @Suppress("UNCHECKED_CAST")
            val writer = jacksonObjectMapper()
                .apply {
                    serializerProvider.setNullKeySerializer(NullValueClassKeySerializer as JsonSerializer<Any?>)
                    serializerProvider.setNullValueSerializer(NullValueSerializer)
                }.writerWithDefaultPrettyPrinter()

            assertEquals(
                """
                    {
                      "foo" : 0,
                      "bar" : "null-value",
                      "baz" : 0,
                      "qux" : [ 0, "null-value" ],
                      "quux" : [ 0, "null-value" ],
                      "corge" : {
                        "inlineField" : 0
                      },
                      "grault" : "null-value",
                      "garply" : {
                        "0" : 0,
                        "1" : "null-value",
                        "null-key" : "null-value"
                      }
                    }
                """.trimIndent(),
                writer.writeValueAsString(target)
            )
        }
    }

    class SerializerPriorityTest {
        @JvmInline
        value class ValueBySerializer(val value: Int)

        object Serializer : StdSerializer<ValueBySerializer>(ValueBySerializer::class.java) {
            override fun serialize(value: ValueBySerializer, gen: JsonGenerator, provider: SerializerProvider) {
                gen.writeString(value.value.toString())
            }
        }
        object KeySerializer : StdSerializer<ValueBySerializer>(ValueBySerializer::class.java) {
            override fun serialize(value: ValueBySerializer, gen: JsonGenerator, provider: SerializerProvider) {
                gen.writeFieldName(value.value.toString())
            }
        }

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
        @Ignore
        @Test
        fun priorityTest() {
            val km = KotlinModule.Builder().build()
            val om1: ObjectMapper = JsonMapper.builder().addModules(km, sm).build()
            val om2: ObjectMapper = JsonMapper.builder().addModules(sm, km).build()

            // om1(collect) -> """{"1":"2"}"""
            // om2(broken)  -> """{"1":2}"""
            assertEquals(om1.writeValueAsString(target), om2.writeValueAsString(target))
        }
    }
}
