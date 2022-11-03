package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals

// verifying work around for this issue, no bug present

class TestGithub179 {
    val objectMapper = jacksonObjectMapper()

    @Test
    fun listOfStrings() {
        val strings = objectMapper.readValue<Strings>("""[ "first", "second" ]""")
        assertEquals(strings.values, listOf("first", "second"))
    }

    @Test
    fun embeddedListOfStrings() {
        val stringsContainer = objectMapper.readValue<StringsContainer>(
                """{ "strings" : [ "first", "second" ] }""")
        assertEquals(stringsContainer.strings.values, listOf("first", "second"))
    }

    @Test
    fun embeddedListOfEnums() {
        val myEnumsContainer = objectMapper.readValue<MyEnumsContainer>(
                """{ "myEnums" : [ "first", "second" ] }""")
        assertEquals(myEnumsContainer.myEnums.values, listOf(MyEnum.FIRST, MyEnum.SECOND))
    }

    private class StringsContainer(@JsonProperty("strings") val strings: Strings)

    private class MyEnumsContainer(@JsonProperty("myEnums") val myEnums: MyEnums)

    private class Strings @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(val values : List<String>)

    private class MyEnums @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(val values : List<MyEnum>)

    private enum class MyEnum {
        @JsonProperty("first") FIRST,
        @JsonProperty("second") SECOND
    }
}