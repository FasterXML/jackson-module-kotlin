package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertTrue


class TestGithub27 {
    val mapper = jacksonObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, false)

    private data class ClassWithNullableInt(val sample: Int?)

    @Test fun testNullableInt() {
        val json = """{"sample":null}"""
        val stateObj = mapper.readValue<ClassWithNullableInt>(json)
        assertThat(stateObj, equalTo(ClassWithNullableInt(null)))
    }

    private data class ClassWithInt(val sample: Int)

    @Test fun testInt() {
        val json = """{"sample":null}"""
        val stateObj = mapper.readValue<ClassWithInt>(json)
        assertThat(stateObj, equalTo(ClassWithInt(0)))
    }

    private data class ClassWithListOfNullableInt(val samples: List<Int?>)

    @Test fun testListOfNullableInt() {
        val json = """{"samples":[1, null]}"""
        val stateObj = mapper.readValue<ClassWithListOfNullableInt>(json)
        assertThat(stateObj.samples, equalTo(listOf(1, null)))
    }

    private data class ClassWithListOfInt(val samples: List<Int>)

    @Test(expected = MissingKotlinParameterException::class)
    fun testListOfInt() {
        val json = """{"samples":[1, null]}"""
        mapper.readValue<ClassWithListOfInt>(json)
    }

    private data class TestClass<T>(val samples: T)

    @Test fun testListOfGeneric() {
        val json = """{"samples":[1, 2]}"""
        val stateObj = mapper.readValue<TestClass<List<Int>>>(json)
        assertThat(stateObj.samples, equalTo(listOf(1, 2)))
    }

    // work around to above
    private class ClassWithListOfIntProtected(val samples: List<Int>) {
        @get:JsonIgnore val safeSamples: List<Int> by lazy { samples.filterNotNull() }
    }

    // TODO:  this would get tougher to nullable check, tough problem to solve
    class ClassWithNonNullableT<T>(val something: T,
                                   val listSomething: List<T>,
                                   val mapOfListOfSomething: Map<String, List<T>>,
                                   val innerSomething: ClassWithNonNullableT<T>?)
}
