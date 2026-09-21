package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.core.JsonParser.NumberType
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonAnyFormatVisitor
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor
import com.fasterxml.jackson.module.kotlin.defaultMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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
