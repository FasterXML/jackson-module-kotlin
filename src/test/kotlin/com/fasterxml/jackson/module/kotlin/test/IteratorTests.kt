package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Ignore
import org.junit.Test


class TestIteratorSubclass {
    class TinyPerson(val name: String, val age: Int)
    class KotlinPersonIterator(private val personList: List<TinyPerson>) : Iterator<TinyPerson> by personList.iterator() {}

    val mapper: ObjectMapper = jacksonObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, false)


    @Test fun testKotlinIterator() {
        val expectedJson = """[{"name":"Fred","age":10},{"name":"Max","age":11}]"""
        val people = KotlinPersonIterator(listOf(TinyPerson("Fred", 10), TinyPerson("Max", 11)))
        val typeRef = object : TypeReference<Iterator<TinyPerson>>() {}
        val kotlinJson = mapper.writerFor(typeRef).writeValueAsString(people)
        assertThat(kotlinJson, equalTo(expectedJson))
    }

    @Ignore("Failing, but need change in Jackson to allow this to work.")
    @Test fun testKotlinIteratorFails() {
        val expectedJson = """[{"name":"Fred","age":10},{"name":"Max","age":11}]"""
        val people = KotlinPersonIterator(listOf(TinyPerson("Fred", 10), TinyPerson("Max", 11)))
        val kotlinJson = mapper.writeValueAsString(people)
        assertThat(kotlinJson, equalTo(expectedJson))
    }

    class Company(val name: String, @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN") @JsonSerialize(`as` = java.util.Iterator::class) val people: KotlinPersonIterator)

    @Test fun testKotlinIteratorAsField() {
        val expectedJson = """{"name":"KidVille","people":[{"name":"Fred","age":10},{"name":"Max","age":11}]}"""
        val people = KotlinPersonIterator(listOf(TinyPerson("Fred", 10), TinyPerson("Max", 11)))
        val company = Company("KidVille", people)
        val kotlinJson = mapper.writeValueAsString(company)
        assertThat(kotlinJson, equalTo(expectedJson))
    }

}