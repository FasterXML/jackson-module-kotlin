package tools.jackson.module.kotlin.kogeraIntegration.ser.valueClass.serializer.byAnnotation.nonNullObject

import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.testPrettyWriter
import tools.jackson.module.kotlin.kogeraIntegration.ser.valueClass.serializer.NonNullObject
import kotlin.test.Test
import kotlin.test.assertEquals

class ByAnnotationTest {
    companion object {
        val writer = jacksonObjectMapper().testPrettyWriter()
    }

    data class NonNullSrc(
        @get:JsonSerialize(using = NonNullObject.Serializer::class)
        val getterAnn: NonNullObject,
        @field:JsonSerialize(using = NonNullObject.Serializer::class)
        val fieldAnn: NonNullObject
    )

    @Test
    fun nonNull() {
        val src = NonNullSrc(NonNullObject("foo"), NonNullObject("bar"))

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
        @get:JsonSerialize(using = NonNullObject.Serializer::class)
        val getterAnn: NonNullObject?,
        @field:JsonSerialize(using = NonNullObject.Serializer::class)
        val fieldAnn: NonNullObject?
    )

    @Test
    fun nullableWithoutNull() {
        val src = NullableSrc(NonNullObject("foo"), NonNullObject("bar"))

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
