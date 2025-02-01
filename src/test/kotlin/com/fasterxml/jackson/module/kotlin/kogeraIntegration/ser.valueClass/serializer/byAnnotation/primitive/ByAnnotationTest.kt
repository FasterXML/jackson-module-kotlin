package com.fasterxml.jackson.module.kotlin.kogeraIntegration.ser.valueClass.serializer.byAnnotation.primitive

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.testPrettyWriter
import com.fasterxml.jackson.module.kotlin.kogeraIntegration.ser.valueClass.serializer.Primitive
import kotlin.test.Test
import kotlin.test.assertEquals

class ByAnnotationTest {
    companion object {
        val writer = KotlinModule.Builder()
            .build()
            .let { ObjectMapper().registerModule(it) }
            .testPrettyWriter()
    }

    data class NonNullSrc(
        @JsonSerialize(using = Primitive.Serializer::class)
        val paramAnn: Primitive,
        @get:JsonSerialize(using = Primitive.Serializer::class)
        val getterAnn: Primitive,
        @field:JsonSerialize(using = Primitive.Serializer::class)
        val fieldAnn: Primitive
    )

    @Test
    fun nonNull() {
        val src = NonNullSrc(Primitive(0), Primitive(1), Primitive(2))

        assertEquals(
            """
                {
                  "paramAnn" : 100,
                  "getterAnn" : 101,
                  "fieldAnn" : 102
                }
            """.trimIndent(),
            writer.writeValueAsString(src)
        )
    }

    data class NullableSrc(
        @JsonSerialize(using = Primitive.Serializer::class)
        val paramAnn: Primitive?,
        @get:JsonSerialize(using = Primitive.Serializer::class)
        val getterAnn: Primitive?,
        @field:JsonSerialize(using = Primitive.Serializer::class)
        val fieldAnn: Primitive?
    )

    @Test
    fun nullableWithoutNull() {
        val src = NullableSrc(Primitive(0), Primitive(1), Primitive(2))

        assertEquals(
            """
                {
                  "paramAnn" : 100,
                  "getterAnn" : 101,
                  "fieldAnn" : 102
                }
            """.trimIndent(),
            writer.writeValueAsString(src)
        )
    }

    @Test
    fun nullableWithNull() {
        val src = NullableSrc(null, null, null)

        assertEquals(
            """
                {
                  "paramAnn" : null,
                  "getterAnn" : null,
                  "fieldAnn" : null
                }
            """.trimIndent(),
            writer.writeValueAsString(src)
        )
    }
}
