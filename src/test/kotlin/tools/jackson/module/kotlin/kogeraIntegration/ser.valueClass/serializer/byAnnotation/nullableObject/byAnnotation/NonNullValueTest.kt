package tools.jackson.module.kotlin.kogeraIntegration.ser.valueClass.serializer.byAnnotation.nullableObject.byAnnotation

import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.testPrettyWriter
import tools.jackson.module.kotlin.kogeraIntegration.ser.valueClass.serializer.NullableObject
import kotlin.test.Test
import kotlin.test.assertEquals

class NonNullValueTest {
    companion object {
        val writer = jacksonObjectMapper().testPrettyWriter()
    }

    data class NonNullSrc(
        @get:JsonSerialize(using = NullableObject.Serializer::class)
        val getterAnn: NullableObject,
        @field:JsonSerialize(using = NullableObject.Serializer::class)
        val fieldAnn: NullableObject
    )

    @Test
    fun nonNull() {
        val src = NonNullSrc(NullableObject("foo"), NullableObject("bar"))

        assertEquals(
            """
                {
                  "getterAnn" : "foo-ser",
                  "fieldAnn" : "bar-ser"
                }
            """.trimIndent(),
            writer.writeValueAsString(src)
        )
    }

    data class NullableSrc(
        @get:JsonSerialize(using = NullableObject.Serializer::class)
        val getterAnn: NullableObject?,
        @field:JsonSerialize(using = NullableObject.Serializer::class)
        val fieldAnn: NullableObject?
    )

    @Test
    fun nullableWithoutNull() {
        val src = NullableSrc(NullableObject("foo"), NullableObject("bar"))

        assertEquals(
            """
                {
                  "getterAnn" : "foo-ser",
                  "fieldAnn" : "bar-ser"
                }
            """.trimIndent(),
            writer.writeValueAsString(src)
        )
    }

    @Test
    fun nullableWithNull() {
        val src = NullableSrc(null, null)

        assertEquals(
            """
                {
                  "getterAnn" : null,
                  "fieldAnn" : null
                }
            """.trimIndent(),
            writer.writeValueAsString(src)
        )
    }
}
