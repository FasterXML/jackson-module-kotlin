package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals

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

    @Test
    fun deserializeValueClass() {
        val original = ClassWithValueMember(ValueClass("bar"))
        assertEquals(original, mapper.readValue(mapper.writeValueAsString(original)))
    }

    @Test
    fun serializeValueClass() {
        val original = ClassWithValueMember(ValueClass("bar"))
        assertEquals("""{"valueClassProperty":"bar"}""", mapper.writeValueAsString(original))
    }
}

@Suppress("EXPERIMENTAL_FEATURE_WARNING") // Experimental enabled in test-compile
inline class InlineClass(val value: String)

@JsonDeserialize(builder = ClassWithInlineMember.JacksonBuilder::class)
data class ClassWithInlineMember(val inlineClassProperty: InlineClass) {
    data class JacksonBuilder constructor(val inlineClassProperty: String) {
        fun build() = ClassWithInlineMember(InlineClass(inlineClassProperty))
    }
}

@Suppress("EXPERIMENTAL_FEATURE_WARNING") // Enabled in test-compile
@JvmInline
value class ValueClass(val value: String)

@JsonDeserialize(builder = ClassWithValueMember.JacksonBuilder::class)
data class ClassWithValueMember(val valueClassProperty: ValueClass) {
    data class JacksonBuilder constructor(val valueClassProperty: String) {
        fun build() = ClassWithValueMember(ValueClass(valueClassProperty))
    }
}
