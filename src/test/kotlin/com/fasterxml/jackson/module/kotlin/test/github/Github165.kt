package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestGithub165 {
    class Github165KotlinTest(@JsonProperty("name") var showName: String)
    {
        var yearSetterCalled: Boolean = false;
        var nameSetterCalled: Boolean = false;

        @JsonProperty("year") lateinit var showYear: String

        @JsonSetter("year")
        fun setYear(value: String)
        {
            yearSetterCalled = true
            this.showYear = value
        }

        @JsonSetter("name")
        fun setName(value: String)
        {
            nameSetterCalled = true
            this.showName = value
        }
    }

    @Test
    fun testJsonSetterCalledKotlin() {
        val obj = jacksonObjectMapper().readValue<Github165KotlinTest>("""{"name":"Fred","year":"1942"}""")
        assertEquals("1942", obj.showYear)
        assertEquals("Fred", obj.showName)
        assertTrue(obj.yearSetterCalled)
        assertFalse(obj.nameSetterCalled)
    }

    @Test
    fun testJsonSetterCalledJava() {
        val obj = jacksonObjectMapper().readValue<Github165JavaTest>("""{"name":"Fred","year":"1942"}""")
        assertEquals("1942", obj.showYear)
        assertEquals("Fred", obj.showName)
        assertTrue(obj.yearSetterCalled)
        assertFalse(obj.nameSetterCalled)
    }
}