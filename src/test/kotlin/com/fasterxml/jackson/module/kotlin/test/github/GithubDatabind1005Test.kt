package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test

class TestGithubDatabind1005 {

    // make a class seem kinda like a Java class, but with a second generated constructor
    // this is Ugly Kotlin but is making it easier for Java people to see and debug.
    class Something @JsonCreator constructor(@JsonProperty("name") name: String,
                                             @JsonProperty("age") age: Int = 0) { // default value causes synthetic constructor to be generated
        val name: String = name
        val age: Int = age
    }

    @Test fun testSyntheticGeneratedConstructorIsIgnored() {
        ObjectMapper().readValue<Something>("""{"name":"fred","age":99}""")
    }
}
