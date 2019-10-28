package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.Test
import java.util.EnumSet
import kotlin.test.assertEquals

class NullToEmptyCollectionTest {

    private data class TestClass(val foo: List<Int>)

    private data class TestClassWithEnumSet(val foo: EnumSet<TestEnum>)

    private enum class TestEnum { Foo, Bar }

    @Test
    fun nonNullCaseStillWorks() {
        val mapper = createMapper()
        assertEquals(listOf(1, 2), mapper.readValue("""{"foo": [1,2]}""", TestClass::class.java).foo)
    }

    @Test
    fun shouldMapNullValuesToEmpty() {
        val mapper = createMapper()
        assertEquals(emptyList(), mapper.readValue("{}", TestClass::class.java).foo)
        assertEquals(emptyList(), mapper.readValue("""{"foo": null}""", TestClass::class.java).foo)

    }

    @Test
    fun shouldMapNullValuesToEmptyEnumSet() {
        val mapper = createMapper()
        assertEquals(EnumSet.noneOf(TestEnum::class.java), mapper.readValue("{}", TestClassWithEnumSet::class.java).foo)
        assertEquals(EnumSet.noneOf(TestEnum::class.java), mapper.readValue("""{"foo": null}""", TestClassWithEnumSet::class.java).foo)
    }

    private fun createMapper(): ObjectMapper {
        return ObjectMapper().registerModule(KotlinModule(nullToEmptyCollection = true))
    }
}