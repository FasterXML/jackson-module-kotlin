package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.RuntimeJsonMappingException
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ReadValuesTest {
    class MyStrDeser : StdDeserializer<String>(String::class.java) {
        override fun deserialize(
            p: JsonParser,
            ctxt: DeserializationContext
        ): String? = p.valueAsString.takeIf { it != "bar" }
    }

    @Nested
    inner class CheckTypeMismatchTest {
        val mapper = jacksonObjectMapper().registerModule(
            object : SimpleModule() {
                init {
                    addDeserializer(String::class.java, MyStrDeser())
                }
            }
        )!!

        @Test
        fun readValuesJsonParserNext() {
            val src = mapper.createParser(""""foo"${"\n"}"bar"""")
            val itr = mapper.readValues<String>(src)

            assertEquals("foo", itr.next())
            assertThrows<RuntimeJsonMappingException> {
                itr.next()
            }
        }

        @Test
        fun readValuesJsonParserNextValue() {
            val src = mapper.createParser(""""foo"${"\n"}"bar"""")
            val itr = mapper.readValues<String>(src)

            assertEquals("foo", itr.nextValue())
            assertThrows<JsonMappingException> {
                itr.nextValue()
            }
        }

        @Test
        fun readValuesTypedJsonParser() {
            val reader = mapper.reader()
            val src = reader.createParser(""""foo"${"\n"}"bar"""")
            val itr = reader.readValuesTyped<String>(src)

            assertEquals("foo", itr.next())
            assertThrows<JsonMappingException> {
                itr.next()
            }
        }
    }
}
