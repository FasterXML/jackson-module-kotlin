package tools.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JacksonInject
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import tools.jackson.databind.InjectableValues
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonObjectMapper
import kotlin.math.exp
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Github722 {
    data class FailingDto @JsonCreator constructor(
        @JacksonInject("foo")
        @JsonProperty("foo")
        val foo: Int = 100,
        @JacksonInject("bar")
        @JsonProperty("bar")
        val bar: Int? = 200
    )

    val injectValues = mapOf("foo" to 1, "bar" to 2)
    val expected = FailingDto(1, 2)

    @Test
    fun onPlainMapper() {
        // Succeeds in plain mapper
        val plainMapper = ObjectMapper()
        assertEquals(
            expected,
            plainMapper.readerFor(FailingDto::class.java)
                .with(InjectableValues.Std(injectValues))
                .readValue("{}")
        )
    }

    @Test
    fun failing() {
        // The kotlin mapper uses the Kotlin default value instead of the Inject value.
        val kotlinMapper = jacksonObjectMapper()
        val result = kotlinMapper.readerFor(FailingDto::class.java)
            .with(InjectableValues.Std(injectValues))
            .readValue<FailingDto>("{}")

        assertEquals(expected, result)
    }

    data class WithoutDefaultValue(
        @JacksonInject("foo")
        val foo: Int,
        @JacksonInject("bar")
        val bar: Int?
    )

    @Test
    fun withoutDefaultValue() {
        val kotlinMapper = jacksonObjectMapper()
        val result = kotlinMapper.readerFor(WithoutDefaultValue::class.java)
            .with(InjectableValues.Std(injectValues))
            .readValue<WithoutDefaultValue>("{}")

        // If there is no default value, the problem does not occur.
        assertEquals(WithoutDefaultValue(1, 2), result)
    }
}
