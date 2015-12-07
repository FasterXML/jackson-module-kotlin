package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.annotation.*
import org.junit.Ignore
import org.junit.Test

public class TestHiddenKotlinThings {

    // make a class seem kinda like a Java class, but with a second generated constructor
    // this is Ugly Kotlin but is making it easier for Java people to see and debug.
    class Something @JsonCreator constructor (@JsonProperty("name") name: String,
                                              @JsonProperty("age") age: Int = 0) { // default value causes synthetic constructor to be generated
        public val name: String = name
        public val age: Int = age
    }

    @Test public fun testSyntheticGeneratedConstructorIsIgnored() {
       val thing: Something = ObjectMapper().readValue("""{"name":"fred","age":99}""")
    }
}
