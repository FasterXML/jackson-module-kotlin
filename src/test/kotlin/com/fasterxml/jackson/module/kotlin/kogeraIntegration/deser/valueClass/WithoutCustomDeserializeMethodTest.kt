package com.fasterxml.jackson.module.kotlin.kogeraIntegration.deser.valueClass

import com.fasterxml.jackson.module.kotlin.defaultMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import java.lang.reflect.InvocationTargetException
import org.junit.jupiter.api.Test

@RunWith(Enclosed::class)
class WithoutCustomDeserializeMethodTest {
    @Ignore
    companion object {
        val mapper = jacksonObjectMapper()
        val throwable = IllegalArgumentException("test")
    }

    class DirectDeserializeTest {
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
        class NullableObject_ {
            @Test
            fun value() {
                val result = defaultMapper.readValue<NullableObject>(""""foo"""")
                assertEquals(NullableObject("foo"), result)
            }

            // failing
            @Test
            fun nullString() {
                // #209 has been fixed.
                assertThrows(NullPointerException::class.java) {
                    val result = defaultMapper.readValue<NullableObject>("null")
                    assertEquals(NullableObject(null), result)
                }
            }
        }

        @Ignore
        @JvmInline
        value class HasCheckConstructor(val value: Int) {
            init {
                if (value < 0) throw throwable
            }
        }

        @Test
        fun callConstructorCheckTest() {
            val e = assertThrows(InvocationTargetException::class.java) {
                defaultMapper.readValue<HasCheckConstructor>("-1")
            }
            assertTrue(e.cause === throwable)
        }
    }

    @Ignore
    data class Dst(
        val pNn: Primitive,
        val pN: Primitive?,
        val nnoNn: NonNullObject,
        val nnoN: NonNullObject?,
        val noNn: NullableObject,
        val noN: NullableObject?
    )

    class InParameterDeserialize {
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
    }

    // If all JsonCreator tests are OK, no need to check throws from factory functions.
}
