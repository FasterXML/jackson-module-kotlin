package com.fasterxml.jackson.module.kotlin.test.github.failing

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Fields named "is…" are only serialized if they are Boolean
 */
class TestGitHub337 {
    private val mapper = jsonMapper {
//        enable(JsonInclude.Include.ALWAYS)
        enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
    }
//            .setSerializationInclusion(JsonInclude.Include.ALWAYS)
//            .configure(SORT_PROPERTIES_ALPHABETICALLY, true)
    private val writer = mapper.writerWithDefaultPrettyPrinter()

    @Test
    @Ignore
    fun test_ClassWithIsFields() {
        data class ClassWithIsFields(
                val isBooleanField: Boolean,
                val isIntField: Int
        )

        val problematic = ClassWithIsFields(true, 9)
        val expected = """
        {
          "isBooleanField" : true,
          "isIntField" : 9
        }""".trimIndent()
        assertEquals(expected, writer.writeValueAsString(problematic))
    }

    @Test
    @Ignore
    fun test_AnnotatedClassWithIsFields() {
        data class ClassWithIsFields(
                @JsonProperty("isBooleanField") val isBooleanField: Boolean,
                @JsonProperty("isIntField") val isIntField: Int
        )

        val problematic = ClassWithIsFields(true, 9)
        val expected = """
        {
          "isBooleanField" : true,
          "isIntField" : 9
        }""".trimIndent()
        assertEquals(expected, writer.writeValueAsString(problematic))
    }

    @Test
    fun test_AnnotatedGetClassWithIsFields() {
        data class ClassWithIsFields(
                @JsonProperty("isBooleanField") val isBooleanField: Boolean,
                @get:JsonProperty("isIntField") val isIntField: Int
        )

        val problematic = ClassWithIsFields(true, 9)
        val expected = """
        {
          "booleanField" : true,
          "isIntField" : 9
        }""".trimIndent()
        assertEquals(expected, writer.writeValueAsString(problematic))
    }
}