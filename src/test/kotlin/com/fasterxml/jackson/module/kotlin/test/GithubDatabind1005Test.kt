package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.annotation.*
import org.junit.Ignore
import org.junit.Test

class TestHiddenKotlinThings {

    // make a class seem kinda like a Java class, but with a second generated constructor
    // this is Ugly Kotlin but is making it easier for Java people to see and debug.
    class Something @JsonCreator constructor(@JsonProperty("name") name: String,
                                             @JsonProperty("age") age: Int = 0) { // default value causes synthetic constructor to be generated
        val name: String = name
        val age: Int = age
    }

    @Ignore("This isn't a problem with the Kotlin module unless someone refuses to use it, then constructor selection gets confused over synthetic constructors")
    @Test public fun testSyntheticGeneratedConstructorIsIgnored() {
        val thing: Something = ObjectMapper().readValue("""{"name":"fred","age":99}""")
    }
}
