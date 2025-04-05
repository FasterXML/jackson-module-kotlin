package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.node.NullNode
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.StringReader

class ReadValueTest {
    @Nested
    inner class CheckTypeMismatchTest {
        @Test
        fun jsonParser() {
            val src = defaultMapper.createParser("null")
            assertThrows<JsonMappingException> {
                defaultMapper.readValue<String>(src)
            }
        }

        @Test
        fun file() {
            val src = createTempJson("null")
            assertThrows<JsonMappingException> {
                defaultMapper.readValue<String>(src)
            }
        }

        // Not implemented because a way to test without mocks was not found
        // @Test
        // fun url() {
        // }

        @Test
        fun string() {
            val src = "null"
            assertThrows<JsonMappingException> {
                defaultMapper.readValue<String>(src)
            }
        }

        @Test
        fun reader() {
            val src = StringReader("null")
            assertThrows<JsonMappingException> {
                defaultMapper.readValue<String>(src)
            }
        }

        @Test
        fun inputStream() {
            val src = "null".byteInputStream()
            assertThrows<JsonMappingException> {
                defaultMapper.readValue<String>(src)
            }
        }

        @Test
        fun byteArray() {
            val src = "null".toByteArray()
            assertThrows<JsonMappingException> {
                defaultMapper.readValue<String>(src)
            }
        }

        @Test
        fun treeToValueTreeNode() {
            assertThrows<JsonMappingException> {
                defaultMapper.treeToValue<String>(NullNode.instance)
            }
        }

        @Test
        fun convertValueAny() {
            assertThrows<JsonMappingException> {
                defaultMapper.convertValue<String>(null)
            }
        }

        @Test
        fun readValueTypedJsonParser() {
            val reader = defaultMapper.reader()
            val src = reader.createParser("null")
            assertThrows<JsonMappingException> {
                reader.readValueTyped<String>(src)
            }
        }
    }
}
