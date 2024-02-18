package com.fasterxml.jackson.module.kotlin.kogeraIntegration.deser.valueClass.deserializer.byAnnotation.specifiedForProperty

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.kogeraIntegration.deser.valueClass.NonNullObject
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import kotlin.test.Test

@RunWith(Enclosed::class)
class NonNullObjectTest {
    @Ignore
    companion object {
        val mapper = jacksonObjectMapper()
    }

    @Ignore
    data class NonNull(
        @get:JsonDeserialize(using = NonNullObject.Deserializer::class)
        val getterAnn: NonNullObject,
        @field:JsonDeserialize(using = NonNullObject.Deserializer::class)
        val fieldAnn: NonNullObject
    )

    class NonNullTest {
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
    }

    @Ignore
    data class Nullable(
        @get:JsonDeserialize(using = NonNullObject.Deserializer::class)
        val getterAnn: NonNullObject?,
        @field:JsonDeserialize(using = NonNullObject.Deserializer::class)
        val fieldAnn: NonNullObject?
    )

    class NullableTest {
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
