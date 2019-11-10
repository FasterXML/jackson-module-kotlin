package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JacksonInject
import com.fasterxml.jackson.databind.InjectableValues
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import java.util.UUID
import kotlin.test.assertEquals

class TestGithub101_JacksonInjectTest {
    @Test
    fun `JacksonInject-annotated parameters are populated when constructing Kotlin data classes`() {
        val mapper = jacksonObjectMapper()
        val contextualValue = UUID.randomUUID()
        assertEquals(SomeDatum("test", contextualValue),
                mapper.readerFor(SomeDatum::class.java)
                        .with(InjectableValues.Std(mapOf("context" to contextualValue)))
                        .readValue("""{ "value": "test" }"""))
    }

    data class SomeDatum(val value: String, @JacksonInject("context") val contextualValue: UUID)
}