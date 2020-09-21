package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TestGithub356 {
    private val mapper = jacksonObjectMapper()

    @Test
    fun deserializeInlineClass() {
        val original = ClassWithInlineMember(InlineClass("bar"))
        assertEquals(original, mapper.readValue(mapper.writeValueAsString(original)))
    }

    @Test
    fun serializeInlineClass() {
        val original = ClassWithInlineMember(InlineClass("bar"))
        assertEquals("""{"inlineClassProperty":"bar"}""", mapper.writeValueAsString(original))
    }
}

@Suppress("EXPERIMENTAL_FEATURE_WARNING") // Enabled in test-compile
inline class InlineClass(val value: String)

@JsonDeserialize(builder = ClassWithInlineMember.JacksonBuilder::class)
data class ClassWithInlineMember(val inlineClassProperty: InlineClass) {
    data class JacksonBuilder constructor(val inlineClassProperty: String) {
        fun build() = ClassWithInlineMember(InlineClass(inlineClassProperty))
    }
}
