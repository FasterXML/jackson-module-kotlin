package tools.jackson.module.kotlin.test

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import tools.jackson.core.JsonParser.NumberType
import tools.jackson.databind.JavaType
import tools.jackson.databind.jsonFormatVisitors.JsonAnyFormatVisitor
import tools.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper
import tools.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor
import tools.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor
import tools.jackson.module.kotlin.defaultMapper

/**
 * The unsigned integer serializers report the integer format to schema generators
 * (previously "any", the StdSerializer default).
 */
internal class UnsignedNumbersJsonFormatVisitorTest {
    private class RecordingVisitor : JsonFormatVisitorWrapper.Base() {
        var format: String? = null
        var numberType: NumberType? = null

        override fun expectIntegerFormat(type: JavaType): JsonIntegerFormatVisitor {
            format = "integer"

            return object : JsonIntegerFormatVisitor.Base() {
                override fun numberType(type: NumberType) {
                    numberType = type
                }
            }
        }

        override fun expectAnyFormat(type: JavaType): JsonAnyFormatVisitor? {
            format = "any"
            return super.expectAnyFormat(type)
        }

        override fun expectStringFormat(type: JavaType): JsonStringFormatVisitor? {
            format = "string"
            return super.expectStringFormat(type)
        }
    }

    private fun visit(type: Class<*>): RecordingVisitor =
        RecordingVisitor().also { defaultMapper.acceptJsonFormatVisitor(type, it) }

    private fun assertInteger(type: Class<*>, numberType: NumberType) {
        val visitor = visit(type)

        assertEquals("integer", visitor.format)
        assertEquals(numberType, visitor.numberType)
    }

    @Test
    fun `UByte is an integer`() = assertInteger(UByte::class.java, NumberType.INT)

    @Test
    fun `UShort is an integer`() = assertInteger(UShort::class.java, NumberType.INT)

    @Test
    fun `UInt is an integer`() = assertInteger(UInt::class.java, NumberType.LONG)

    @Test
    fun `ULong is an integer`() = assertInteger(ULong::class.java, NumberType.BIG_INTEGER)
}
