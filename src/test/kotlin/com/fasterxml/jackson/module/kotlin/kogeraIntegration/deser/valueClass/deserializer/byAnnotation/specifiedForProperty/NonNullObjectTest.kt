package com.fasterxml.jackson.module.kotlin.kogeraIntegration.deser.valueClass.deserializer.byAnnotation.specifiedForProperty

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.kogeraIntegration.deser.valueClass.NonNullObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class NonNullObjectTest {
    companion object {
        val mapper = jacksonObjectMapper()
    }

    data class NonNull(
        @get:JsonDeserialize(using = NonNullObject.Deserializer::class)
        val getterAnn: NonNullObject,
        @field:JsonDeserialize(using = NonNullObject.Deserializer::class)
        val fieldAnn: NonNullObject
    )

    @Test
    fun nonNull() {
        val result = mapper.readValue<NonNull>(
            """
                {
                  "getterAnn" : "foo",
                  "fieldAnn" : "bar"
                }
            """.trimIndent()
        )
        assertEquals(NonNull(NonNullObject("foo-deser"), NonNullObject("bar-deser")), result)
    }

    data class Nullable(
        @get:JsonDeserialize(using = NonNullObject.Deserializer::class)
        val getterAnn: NonNullObject?,
        @field:JsonDeserialize(using = NonNullObject.Deserializer::class)
        val fieldAnn: NonNullObject?
    )

    @Nested
    inner class NullableTest {
        @Test
        fun nonNullInput() {
            val result = mapper.readValue<Nullable>(
                """
                {
                  "getterAnn" : "foo",
                  "fieldAnn" : "bar"
                }
                """.trimIndent()
            )
            assertEquals(Nullable(NonNullObject("foo-deser"), NonNullObject("bar-deser")), result)
        }

        @Test
        fun nullInput() {
            val result = mapper.readValue<Nullable>(
                """
                {
                  "getterAnn" : null,
                  "fieldAnn" : null
                }
                """.trimIndent()
            )
            assertEquals(Nullable(null, null), result)
        }
    }
}
