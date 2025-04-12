package com.fasterxml.jackson.module.kotlin.kogeraIntegration.deser.valueClass

import com.fasterxml.jackson.module.kotlin.defaultMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Test
import java.lang.reflect.InvocationTargetException
import kotlin.test.assertNotEquals

class WithoutCustomDeserializeMethodTest {
    companion object {
        val mapper = jacksonObjectMapper()
        val throwable = IllegalArgumentException("test")
    }

    @Nested
    inner class DirectDeserializeTest {
        @Test
        fun primitive() {
            val result = defaultMapper.readValue<Primitive>("1")
            assertEquals(Primitive(1), result)
        }

        @Test
        fun nonNullObject() {
            val result = defaultMapper.readValue<NonNullObject>(""""foo"""")
            assertEquals(NonNullObject("foo"), result)
        }

        @Suppress("ClassName")
        @Nested
        inner class NullableObject_ {
            @Test
            fun value() {
                val result = defaultMapper.readValue<NullableObject>(""""foo"""")
                assertEquals(NullableObject("foo"), result)
            }

            // failing
            @Test
            fun nullString() {
                val result = defaultMapper.readValue<NullableObject?>("null")
                assertNotEquals(NullableObject(null), result, "kogera #209 has been fixed.")
            }
        }
    }

    data class Dst(
        val pNn: Primitive,
        val pN: Primitive?,
        val nnoNn: NonNullObject,
        val nnoN: NonNullObject?,
        val noNn: NullableObject,
        val noN: NullableObject?
    )

    @Test
    fun withoutNull() {
        val expected = Dst(
            Primitive(1),
            Primitive(2),
            NonNullObject("foo"),
            NonNullObject("bar"),
            NullableObject("baz"),
            NullableObject("qux")
        )
        val src = mapper.writeValueAsString(expected)
        val result = mapper.readValue<Dst>(src)

        assertEquals(expected, result)
    }

    @Test
    fun withNull() {
        val expected = Dst(
            Primitive(1),
            null,
            NonNullObject("foo"),
            null,
            NullableObject(null),
            null
        )
        val src = mapper.writeValueAsString(expected)
        val result = mapper.readValue<Dst>(src)

        assertEquals(expected, result)
    }

    @JvmInline
    value class HasCheckConstructor(val value: Int) {
        init {
            if (value < 0) throw throwable
        }
    }

    @Test
    fun callConstructorCheckTest() {
        val e = assertThrows<InvocationTargetException> { defaultMapper.readValue<HasCheckConstructor>("-1") }
        assertTrue(e.cause === throwable)
    }

    // If all JsonCreator tests are OK, no need to check throws from factory functions.
}
