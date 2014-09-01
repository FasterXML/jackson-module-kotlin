package com.fasterxml.jackson.module.kotlin

import org.junit.Test
import org.hamcrest.MatcherAssert.*
import org.hamcrest.CoreMatchers.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.annotation.JsonCreator

class TestJacksonWithKotlinBuiltins {
    private val mapper = jacksonObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, false)!!

    private data class ClassWithPair(val name: Pair<String, String>, val age: Int)
    Test fun testPair() {
        val json = """{"name":{"first":"John","second":"Smith"},"age":30}"""
        val testObj = ClassWithPair(Pair("John", "Smith"), 30)

        assertThat(mapper.writeValueAsString(testObj), equalTo(json))
        val stateObj = mapper.readValue(json, javaClass<ClassWithPair>())
        assertThat(stateObj, equalTo(testObj))
    }

    private data class ClassWithPairMixedTypes(val person: Pair<String, Int>)
    Test fun testPairMixedTypes() {
        val json = """{"person":{"first":"John","second":30}}"""
        val testObj = ClassWithPairMixedTypes(Pair("John", 30))

        assertThat(mapper.writeValueAsString(testObj), equalTo(json))
        val stateObj = mapper.readValue(json, javaClass<ClassWithPairMixedTypes>())
        assertThat(stateObj, equalTo(testObj))
    }

    private data class ClassWithTriple(val name: Triple<String, String, String>, val age: Int)
    Test fun testTriple() {
        val json = """{"name":{"first":"John","second":"Davey","third":"Smith"},"age":30}"""
        val testObj = ClassWithTriple(Triple("John", "Davey", "Smith"), 30)

        assertThat(mapper.writeValueAsString(testObj), equalTo(json))
        val stateObj = mapper.readValue(json, javaClass<ClassWithTriple>())
        assertThat(stateObj, equalTo(testObj))
    }

    private data class ClassWithRanges(val ages: IntRange, val distance: DoubleRange)
    Test fun testRanges() {
        val json = """{"ages":{"start":18,"end":40},"distance":{"start":5.5,"end":50.0}}"""
        val testObj = ClassWithRanges(IntRange(18, 40), DoubleRange(5.5, 50.0))

        assertThat(mapper.writeValueAsString(testObj), equalTo(json))
        val stateObj = mapper.readValue(json, javaClass<ClassWithRanges>())
        assertThat(stateObj, equalTo(testObj))
    }
}
