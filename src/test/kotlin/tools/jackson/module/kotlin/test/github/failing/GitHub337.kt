package tools.jackson.module.kotlin.test.github.failing

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.junit.jupiter.api.Disabled
import tools.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.testPrettyWriter
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Fields named "is…" are only serialized if they are Boolean
 */
class TestGitHub337 {
    private val mapper = jsonMapper {
        enable(SORT_PROPERTIES_ALPHABETICALLY)
        changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.ALWAYS) }
    }
    private val writer = mapper.testPrettyWriter()

    @Test
    @Disabled
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
    @Disabled
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
