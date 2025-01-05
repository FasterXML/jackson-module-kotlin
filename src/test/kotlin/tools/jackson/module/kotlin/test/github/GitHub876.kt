package tools.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tools.jackson.module.kotlin.jacksonMapperBuilder
import kotlin.test.assertEquals

class GitHub876 {
    data class WithAnnotationWithoutDefault(
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        val list: List<String>,
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        val map: Map<String, String>,
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        val string: String
    )

    @Nested
    inner class WithAnnotationWithoutDefaultTest {
        val mapper = jacksonObjectMapper()

        @Test
        fun nullInput() {
            val input = """{"list": null, "map": null, "string": null}"""
            val expected = WithAnnotationWithoutDefault(emptyList(), emptyMap(), "")

            val actual = mapper.readValue<WithAnnotationWithoutDefault>(input)

            assertEquals(expected, actual)
        }

        @Test
        fun undefinedInput() {
            val input = """{}"""
            val expected = WithAnnotationWithoutDefault(emptyList(), emptyMap(), "")

            val actual = mapper.readValue<WithAnnotationWithoutDefault>(input)

            assertEquals(expected, actual)
        }
    }

    data class WithAnnotationWithDefault(
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        val list: List<String> = listOf("default"),
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        val map: Map<String, String> = mapOf("default" to "default"),
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        val string: String = "default"
    )

    @Nested
    inner class WithAnnotationWithDefaultTest {
        val mapper = jacksonObjectMapper()

        @Test
        fun nullInput() {
            // If null is explicitly specified, the default value is not used
            val input = """{"list": null, "map": null, "string": null}"""
            val expected = WithAnnotationWithDefault(emptyList(), emptyMap(), "")

            val actual = mapper.readValue<WithAnnotationWithDefault>(input)

            assertEquals(expected, actual)
        }

        @Test
        fun undefinedInput() {
            // If the input is undefined, the default value is used
            val input = """{}"""
            val expected = WithAnnotationWithDefault()

            val actual = mapper.readValue<WithAnnotationWithDefault>(input)

            assertEquals(expected, actual)
        }
    }

    // If it is set by configOverride, it is treated in the same way as if it were set by annotation
    data class WithoutAnnotationWithoutDefault(
        val list: List<String>,
        val map: Map<String, String>,
        val string: String
    )

    @Nested
    inner class WithoutAnnotationWithoutDefaultTest {
        val mapper = jacksonMapperBuilder()
            .withConfigOverride(List::class.java) { it.nullHandling = JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY) }
            .withConfigOverride(Map::class.java) { it.nullHandling = JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY) }
            .withConfigOverride(String::class.java) { it.nullHandling = JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY) }
            .build()

        @Test
        fun nullInput() {
            val input = """{"list": null, "map": null, "string": null}"""
            val expected = WithoutAnnotationWithoutDefault(emptyList(), emptyMap(), "")

            val actual = mapper.readValue<WithoutAnnotationWithoutDefault>(input)

            assertEquals(expected, actual)
        }

        @Test
        fun undefinedInput() {
            val input = """{}"""
            val expected = WithoutAnnotationWithoutDefault(emptyList(), emptyMap(), "")

            val actual = mapper.readValue<WithoutAnnotationWithoutDefault>(input)

            assertEquals(expected, actual)
        }
    }

    data class WithoutAnnotationWithDefault(
        val list: List<String> = listOf("default"),
        val map: Map<String, String> = mapOf("default" to "default"),
        val string: String = "default"
    )

    @Nested
    inner class WithoutAnnotationWithDefaultTest {
        val mapper = jacksonMapperBuilder()
            .withConfigOverride(List::class.java) { it.nullHandling = JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY) }
            .withConfigOverride(Map::class.java) { it.nullHandling = JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY) }
            .withConfigOverride(String::class.java) { it.nullHandling = JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY) }
            .build()

        @Test
        fun nullInput() {
            val input = """{"list": null, "map": null, "string": null}"""
            val expected = WithoutAnnotationWithDefault(emptyList(), emptyMap(), "")

            val actual = mapper.readValue<WithoutAnnotationWithDefault>(input)

            assertEquals(expected, actual)
        }

        @Test
        fun undefinedInput() {
            val input = """{}"""
            val expected = WithoutAnnotationWithDefault()

            val actual = mapper.readValue<WithoutAnnotationWithDefault>(input)

            assertEquals(expected, actual)
        }
    }
}
