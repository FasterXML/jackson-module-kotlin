package com.fasterxml.jackson.module.kotlin.kogeraIntegration.deser.valueClass.deserializer

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.kogeraIntegration.deser.valueClass.NonNullObject
import com.fasterxml.jackson.module.kotlin.kogeraIntegration.deser.valueClass.NullableObject
import com.fasterxml.jackson.module.kotlin.kogeraIntegration.deser.valueClass.Primitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Ignore
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import kotlin.test.Test

@RunWith(Enclosed::class)
class SpecifiedForObjectMapperTest {
    @Ignore
    companion object {
        val mapper = jacksonObjectMapper().apply {
            val module = SimpleModule().apply {
                this.addDeserializer(Primitive::class.java, Primitive.Deserializer())
                this.addDeserializer(NonNullObject::class.java, NonNullObject.Deserializer())
                this.addDeserializer(NullableObject::class.java, NullableObject.DeserializerWrapsNullable())
            }
            this.registerModule(module)
        }
    }

    class DirectDeserialize {
        @Test
        fun primitive() {
            val result = mapper.readValue<Primitive>("1")
            assertEquals(Primitive(101), result)
        }

        @Test
        fun nonNullObject() {
            val result = mapper.readValue<NonNullObject>(""""foo"""")
            assertEquals(NonNullObject("foo-deser"), result)
        }

        @Suppress("ClassName")class NullableObject_ {
            @Test
            fun value() {
                val result = mapper.readValue<NullableObject>(""""foo"""")
                assertEquals(NullableObject("foo-deser"), result)
            }

            // failing
            @Test
            fun nullString() {
                // #209 has been fixed.
                assertThrows(NullPointerException::class.java) {
                    val result = mapper.readValue<NullableObject>("null")
                    assertEquals(NullableObject("null-value-deser"), result)
                }
            }
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

    @Test
    fun nonNull() {
        val base = Dst(
            Primitive(1),
            Primitive(2),
            NonNullObject("foo"),
            NonNullObject("bar"),
            NullableObject("baz"),
            NullableObject("qux")
        )
        val src = mapper.writeValueAsString(base)
        val result = mapper.readValue<Dst>(src)

        val expected = Dst(
            Primitive(101),
            Primitive(102),
            NonNullObject("foo-deser"),
            NonNullObject("bar-deser"),
            NullableObject("baz-deser"),
            NullableObject("qux-deser")
        )
        assertEquals(expected, result)
    }

    @Test
    fun withNull() {
        val base = Dst(
            Primitive(1),
            null,
            NonNullObject("foo"),
            null,
            NullableObject(null),
            null
        )
        val src = mapper.writeValueAsString(base)
        val result = mapper.readValue<Dst>(src)

        val expected = Dst(
            Primitive(101),
            null,
            NonNullObject("foo-deser"),
            null,
            NullableObject("null-value-deser"),
            null
        )
        assertEquals(expected, result)
    }
}
