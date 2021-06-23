package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.test.expectFailure
import org.junit.ComparisonFailure
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals

class Github464 {
    class UnboxTest {
        val writer = jacksonObjectMapper().writerWithDefaultPrettyPrinter()

        @JvmInline
        value class ValueClass(val value: Int)
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
            val garply: Map<ValueClass, ValueClass?>,
            val waldo: Map<WrapperClass, WrapperClass?>
        )

        // TODO: Remove this function after applying unbox to key of Map and cancel Ignore of test.
        @Test
        fun tempTest() {
            val zeroValue = ValueClass(0)

            val target = Poko(
                foo = zeroValue,
                bar = null,
                baz = zeroValue,
                qux = listOf(zeroValue, null),
                quux = arrayOf(zeroValue, null),
                corge = WrapperClass(zeroValue),
                grault = null,
                garply = emptyMap(),
                waldo = emptyMap()
            )

            assertEquals("""
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
                  "garply" : { },
                  "waldo" : { }
                }
            """.trimIndent(),
                writer.writeValueAsString(target)
            )
        }

        @Test
        fun test() {
            val zeroValue = ValueClass(0)
            val oneValue = ValueClass(1)

            val target = Poko(
                foo = zeroValue,
                bar = null,
                baz = zeroValue,
                qux = listOf(zeroValue, null),
                quux = arrayOf(zeroValue, null),
                corge = WrapperClass(zeroValue),
                grault = null,
                garply = mapOf(zeroValue to zeroValue, oneValue to null),
                waldo = mapOf(WrapperClass(zeroValue) to WrapperClass(zeroValue), WrapperClass(oneValue) to null)
            )

            expectFailure<ComparisonFailure>("GitHub #469 has been fixed!") {
                assertEquals("""
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
                    "1" : null
                  },
                  "waldo" : {
                    "{inlineField=0}" : {
                      "inlineField" : 0
                    },
                    "{inlineField=1}" : null
                  }
                }
            """.trimIndent(),
                    writer.writeValueAsString(target)
                )
            }
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

        private val target = listOf(ValueBySerializer(1))

        @Test
        fun simpleTest() {
            val sm = SimpleModule().addSerializer(Serializer)
            val om: ObjectMapper = jacksonMapperBuilder().addModule(sm).build()

            assertEquals("""["1"]""", om.writeValueAsString(target))
        }

        // Currently, there is a situation where the serialization results are different depending on the registration order of the modules.
        // This problem is not addressed because the serializer registered by the user has priority over Extensions.kt,
        // since KotlinModule is basically registered first.
        @Ignore
        @Test
        fun priorityTest() {
            val sm = SimpleModule().addSerializer(Serializer)
            val km = KotlinModule.Builder().build()
            val om1: ObjectMapper = JsonMapper.builder().addModules(km, sm).build()
            val om2: ObjectMapper = JsonMapper.builder().addModules(sm, km).build()

            // om1(collect) -> """["1"]"""
            // om2(broken)  -> """[1]"""
            assertEquals(om1.writeValueAsString(target), om2.writeValueAsString(target))
        }
    }
}
