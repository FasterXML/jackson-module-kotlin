package com.fasterxml.jackson.module.kotlin.kogeraIntegration.deser.valueClass.mapKey.keyDeserializer

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.kogeraIntegration.deser.valueClass.NonNullObject
import com.fasterxml.jackson.module.kotlin.kogeraIntegration.deser.valueClass.NullableObject
import com.fasterxml.jackson.module.kotlin.kogeraIntegration.deser.valueClass.Primitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SpecifiedForObjectMapperTest {
    companion object {
        val mapper = jacksonObjectMapper().apply {
            val module = SimpleModule().apply {
                this.addKeyDeserializer(Primitive::class.java, Primitive.KeyDeserializer())
                this.addKeyDeserializer(NonNullObject::class.java, NonNullObject.KeyDeserializer())
                this.addKeyDeserializer(NullableObject::class.java, NullableObject.KeyDeserializer())
            }
            this.registerModule(module)
        }
    }

    @Nested
    inner class DirectDeserialize {
        @Test
        fun primitive() {
            val result = mapper.readValue<Map<Primitive, String?>>("""{"1":null}""")
            assertEquals(mapOf(Primitive(101) to null), result)
        }

        @Test
        fun nonNullObject() {
            val result = mapper.readValue<Map<NonNullObject, String?>>("""{"foo":null}""")
            assertEquals(mapOf(NonNullObject("foo-deser") to null), result)
        }

        @Test
        fun nullableObject() {
            val result = mapper.readValue<Map<NullableObject, String?>>("""{"bar":null}""")
            assertEquals(mapOf(NullableObject("bar-deser") to null), result)
        }
    }

    data class Dst(
        val p: Map<Primitive, String?>,
        val nn: Map<NonNullObject, String?>,
        val n: Map<NullableObject, String?>
    )

    @Test
    fun wrapped() {
        val src = """
            {
              "p":{"1":null},
              "nn":{"foo":null},
              "n":{"bar":null}
            }
        """.trimIndent()
        val result = mapper.readValue<Dst>(src)
        val expected = Dst(
            mapOf(Primitive(101) to null),
            mapOf(NonNullObject("foo-deser") to null),
            mapOf(NullableObject("bar-deser") to null)
        )

        assertEquals(expected, result)
    }
}
