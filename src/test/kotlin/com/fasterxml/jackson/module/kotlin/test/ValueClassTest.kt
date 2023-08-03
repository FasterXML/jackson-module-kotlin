package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.junit.Assert
import org.junit.Test

class ValueClassTest {
    @JvmInline
    value class TestClass(val foo: List<Int>)

    @Test
    fun `test value class`() {
        val mapper = createMapper()
        Assert.assertEquals(listOf(1, 2), mapper.readValue("""{"foo": [1,2]}""", TestClass::class.java).foo)
    }

    private fun createMapper(): ObjectMapper {
        return ObjectMapper().registerModule(kotlinModule())
    }
}
