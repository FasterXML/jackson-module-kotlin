package tools.jackson.module.kotlin.kogeraIntegration.ser.valueClass.serializer.byAnnotation.primitive

import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.testPrettyWriter
import tools.jackson.module.kotlin.kogeraIntegration.ser.valueClass.serializer.Primitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ByAnnotationTest {
    companion object {
        val writer = jacksonObjectMapper().testPrettyWriter()
    }

    data class NonNullFailingSrc(
        @JsonSerialize(using = Primitive.Serializer::class)
        val paramAnn: Primitive,
    )

    @Test
    fun nonNullFailing() {
        val src = NullableFailingSrc(Primitive(0))

        assertNotEquals(
            """
                {
                  "paramAnn" : 100
                }
            """.trimIndent(),
            writer.writeValueAsString(src),
            "#651 fixed, it needs to be modified to match the original."
        )
    }

    data class NonNullSrc(
        @get:JsonSerialize(using = Primitive.Serializer::class)
        val getterAnn: Primitive,
        @field:JsonSerialize(using = Primitive.Serializer::class)
        val fieldAnn: Primitive
    )

    @Test
    fun nonNull() {
        val src = NonNullSrc(Primitive(1), Primitive(2))

        assertEquals(
            """
                {
                  "getterAnn" : 101,
                  "fieldAnn" : 102
                }
            """.trimIndent(),
            writer.writeValueAsString(src)
        )
    }

    data class NullableFailingSrc(
        @JsonSerialize(using = Primitive.Serializer::class)
        val paramAnn: Primitive?,
    )

    @Test
    fun nullableFailing() {
        val src = NullableFailingSrc(Primitive(0))

        assertNotEquals(
            """
                {
                  "paramAnn" : 100
                }
            """.trimIndent(),
            writer.writeValueAsString(src),
            "#651 fixed, it needs to be modified to match the original."
        )
    }


    data class NullableSrc(
        @get:JsonSerialize(using = Primitive.Serializer::class)
        val getterAnn: Primitive?,
        @field:JsonSerialize(using = Primitive.Serializer::class)
        val fieldAnn: Primitive?
    )

    @Test
    fun nullableWithoutNull() {
        val src = NullableSrc(Primitive(1), Primitive(2))

        assertEquals(
            """
                {
                  "getterAnn" : 101,
                  "fieldAnn" : 102
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
