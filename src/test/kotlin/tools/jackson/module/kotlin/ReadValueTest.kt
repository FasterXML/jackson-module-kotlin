package tools.jackson.module.kotlin

import tools.jackson.databind.DatabindException
import tools.jackson.databind.node.NullNode
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
            assertThrows<DatabindException> {
                defaultMapper.readValue<String>(src)
            }.printStackTrace()
        }

        @Test
        fun file() {
            val src = createTempJson("null")
            assertThrows<DatabindException> {
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
            assertThrows<DatabindException> {
                defaultMapper.readValue<String>(src)
            }
        }

        @Test
        fun reader() {
            val src = StringReader("null")
            assertThrows<DatabindException> {
                defaultMapper.readValue<String>(src)
            }
        }

        @Test
        fun inputStream() {
            val src = "null".byteInputStream()
            assertThrows<DatabindException> {
                defaultMapper.readValue<String>(src)
            }
        }

        @Test
        fun byteArray() {
            val src = "null".toByteArray()
            assertThrows<DatabindException> {
                defaultMapper.readValue<String>(src)
            }
        }

        @Test
        fun treeToValueTreeNode() {
            assertThrows<DatabindException> {
                defaultMapper.treeToValue<String>(NullNode.instance)
            }
        }

        @Test
        fun convertValueAny() {
            assertThrows<DatabindException> {
                defaultMapper.convertValue<String>(null)
            }
        }

        @Test
        fun readValueTypedJsonParser() {
            val reader = defaultMapper.reader()
            val src = reader.createParser("null")
            assertThrows<DatabindException> {
                reader.readValueTyped<String>(src)
            }
        }
    }
}
