package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.module.kotlin.*
import org.junit.Test
import kotlin.properties.Delegates
import kotlin.test.assertEquals

class TestGithub25 {
    @JsonPropertyOrder(alphabetic = true)
    class SomethingWithDelegates(val data: MutableMap<String, String> = hashMapOf()) {
        val name: String by lazy { "fred" }
        @get:JsonIgnore val ignoreMe: String by lazy { "ignored" }
        var changeable: String = "starting value"
        @get:JsonIgnore var otherData1: String by data
        @get:JsonIgnore var otherData2: String by data
        @get:JsonIgnore val otherData3: String by data
        var somethingNotNull: String by Delegates.notNull()

        fun withOtherData(value: String): SomethingWithDelegates {
            somethingNotNull = value
            return this
        }
    }

    @Test fun testSerWithDelegates() {
        val json = jacksonObjectMapper().writeValueAsString(SomethingWithDelegates(linkedMapOf("otherData1" to "1", "otherData2" to "2", "otherData3" to "3"))
                .withOtherData("exists"))
        assertEquals("""{"data":{"otherData1":"1","otherData2":"2","otherData3":"3"},"changeable":"starting value","name":"fred","somethingNotNull":"exists"}""", json)
    }

    @Test fun testDeserWithDelegates() {
        val json = """{"changeable":"new value","data":{"otherData1":"1","otherData2":"2","otherData3":"3"},"somethingNotNull":"exists"}"""
        val obj: SomethingWithDelegates = jacksonObjectMapper().readValue(json)
        assertEquals("fred", obj.name) // not set by the Json, isn't in the constructor and is read only delegate
        assertEquals("ignored", obj.ignoreMe)
        assertEquals("new value", obj.changeable)
        assertEquals("1", obj.otherData1)
        assertEquals("2", obj.otherData2)
        assertEquals("3", obj.otherData3)
        assertEquals("exists", obj.somethingNotNull)
    }

}