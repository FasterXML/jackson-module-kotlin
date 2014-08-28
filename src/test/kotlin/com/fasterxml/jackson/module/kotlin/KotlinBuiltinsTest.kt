package com.fasterxml.jackson.module.kotlin

import org.junit.Test
import org.hamcrest.MatcherAssert.*
import org.hamcrest.CoreMatchers.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.annotation.JsonCreator

class TestJacksonWithKotlinBuiltins {
    private data class ClassWithPair [JsonCreator] (val name: Pair<String, String>, val age: Int)
    private data class ClassWithTriple [JsonCreator] (val name: Triple<String, String, String>, val age: Int)
    private data class ClassWithPairMixedTypes [JsonCreator] (val person: Pair<String, Int>)

    private val mapper = run {
        val mapper: ObjectMapper = ObjectMapper()
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false)
        mapper.registerModule(KotlinModule())
        mapper
    }

    Test fun testPair() {
        val json = """{"name":{"first":"John","second":"Smith"},"age":30}"""

        assertThat(mapper.writeValueAsString(ClassWithPair(Pair("John","Smith"),30)), equalTo(json))
        val stateObj = mapper.readValue(json, javaClass<ClassWithPair>())
    }

    Test fun testPairMixedTypes() {
        val json = """{"person":{"first":"John","second":30}}"""

        assertThat(mapper.writeValueAsString(ClassWithPairMixedTypes(Pair("John", 30))), equalTo(json))
        val stateObj = mapper.readValue(json, javaClass<ClassWithPairMixedTypes>())
    }

    Test fun testTriple() {
        val json = """{"name":{"first":"John","second":"Davey","third":"Smith"},"age":30}"""

        assertThat(mapper.writeValueAsString(ClassWithTriple(Triple("John","Davey", "Smith"),30)), equalTo(json))
        val stateObj = mapper.readValue(json, javaClass<ClassWithTriple>())
    }
}
