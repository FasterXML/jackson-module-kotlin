package tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.deserializer.byAnnotation.specifiedForProperty

import tools.jackson.databind.annotation.JsonDeserialize
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.NullableObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class NullableObjectTest {
    companion object {
        val mapper = jacksonObjectMapper()
    }

    data class NonNull(
        @get:JsonDeserialize(using = NullableObject.DeserializerWrapsNullable::class)
        val getterAnn: NullableObject,
        @field:JsonDeserialize(using = NullableObject.DeserializerWrapsNullable::class)
        val fieldAnn: NullableObject
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
        assertEquals(NonNull(NullableObject("foo-deser"), NullableObject("bar-deser")), result)
    }

    data class Nullable(
        @get:JsonDeserialize(using = NullableObject.DeserializerWrapsNullable::class)
        val getterAnn: NullableObject?,
        @field:JsonDeserialize(using = NullableObject.DeserializerWrapsNullable::class)
        val fieldAnn: NullableObject?
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
            assertEquals(Nullable(NullableObject("foo-deser"), NullableObject("bar-deser")), result)
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
