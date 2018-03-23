package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.Test
import kotlin.test.assertEquals

class NullToEmptyMapTest {

    private data class TestClass(val foo: Map<String, Int>)

    @Test
    fun nonNullCaseStillWorks() {
        val mapper = createMapper()
        assertEquals(mapOf("bar" to 1), mapper.readValue("""{"foo": {"bar": 1}}""", TestClass::class.java).foo)
    }

    @Test
    fun shouldMapNullValuesToEmpty() {
        val mapper = createMapper()
        assertEquals(emptyMap(), mapper.readValue("{}", TestClass::class.java).foo)
        assertEquals(emptyMap(), mapper.readValue("""{"foo": null}""", TestClass::class.java).foo)

    }

    private fun createMapper(): ObjectMapper {
        return ObjectMapper().registerModule(KotlinModule(nullToEmptyMap = true))
    }
}