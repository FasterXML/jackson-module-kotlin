package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlin.test.Test
import kotlin.test.assertEquals

class GitHub841 {
    object Foo {
        override fun toString(): String = "Foo()"

        @JvmStatic
        @JsonCreator
        fun deserialize(): Foo {
            return Foo
        }
    }

    private val mapper = ObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
        .registerKotlinModule()

    @Test
    fun shouldDeserializeSimpleObject() {
        val value = Foo
        val serialized = mapper.writeValueAsString(value)
        val deserialized = mapper.readValue<Foo>(serialized)

        assertEquals(value, deserialized)
    }
}
