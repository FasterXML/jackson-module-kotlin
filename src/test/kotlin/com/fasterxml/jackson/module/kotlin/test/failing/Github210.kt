package com.fasterxml.jackson.module.kotlin.test.failing

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test

class ExampleFail1(val stringItem: String, val regexItem: Regex)
class ExampleFail2(val regexItem: Regex, val stringItem: String)

class ExampleNoFail(val regexItem: RegexLike, val stringItem: String)
class RegexLike(val pattern: String, val options: List<String>)

class TestGithub210 {
    val mapper = jacksonObjectMapper()

    @Test
    fun testSerDesOfRegex() {
        val happyJson = """{"stringItem":"hello","regexItem":{"options":[],"pattern":"test"}}"""
        val troubleJson = """{"regexItem":{"options":[],"pattern":"test"},"stringItem":"hello"}"""

        mapper.readValue<ExampleNoFail>(happyJson)
        mapper.readValue<ExampleNoFail>(troubleJson)

        mapper.readValue<ExampleFail1>(happyJson)
        mapper.readValue<ExampleFail2>(happyJson)

        // the following fail on stringItem being missing, the KotlinValueInstantiator is confused
        mapper.readValue<ExampleFail1>(troubleJson)       // fail {"regexItem":{"pattern":"test","options":[]},"stringItem":"hello"}
        mapper.readValue<ExampleFail2>(troubleJson)       // fail {"regexItem":{"pattern":"test","options":[]},"stringItem":"hello"}
    }
}