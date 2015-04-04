package com.fasterxml.jackson.module.kotlin.m11

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test


data class DataClassPerson(val name: String, val age: Int)

class M11Tests {
    val mapper = jacksonObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, false)

    class TestPerson1(val name: String, val age: Int)
    Test fun testNormalClass_One_Constructor() {
        val expectedJson = """{"name":"John Smith","age":30}"""
        val expectedPerson = TestPerson1("John Smith", 30)

        val actualJson = mapper.writeValueAsString(expectedPerson)
        val newPerson  = mapper.readValue<TestPerson1>(actualJson)

        assertThat(actualJson, equalTo(expectedJson))
        assertThat(newPerson.name, equalTo(expectedPerson.name))
        assertThat(newPerson.age, equalTo(expectedPerson.age))
    }

    data class TestPerson2(val name: String, val age: Int)
    Test fun testDataClass_One_Constructor() {


        val expectedJson = """{"name":"John Smith","age":30}"""
        val expectedPerson = TestPerson2("John Smith", 30)

        val actualJson = mapper.writeValueAsString(expectedPerson)
        val newPerson  = mapper.readValue<TestPerson2>(actualJson)

        assertThat(actualJson, equalTo(expectedJson))
        assertThat(newPerson, equalTo(expectedPerson))
    }

    data class TestPerson3(val name: String, val age: Int) {
        val otherThing: String
        init {
            otherThing = "franky"
        }
    }
    Test fun testDataClass_Init_Constructor() {

        val expectedJson = """{"name":"John Smith","age":30,"otherThing":"franky"}"""
        val expectedPerson = TestPerson3("John Smith", 30)

        val actualJson = mapper.writeValueAsString(expectedPerson)
        val newPerson  = mapper.readValue<TestPerson3>(actualJson)

        assertThat(actualJson, equalTo(expectedJson))
        assertThat(newPerson, equalTo(expectedPerson))
    }

    data class TestPerson4(val name: String, val age: Int) {
        [JsonIgnore] val otherThing: String
        init {
            otherThing = "franky"
        }
    }
    Test fun testDataClass_Init_Constructor_And_Ignored_Property() {

        val expectedJson = """{"name":"John Smith","age":30}"""
        val expectedPerson = TestPerson4("John Smith", 30)

        val actualJson = mapper.writeValueAsString(expectedPerson)
        val newPerson  = mapper.readValue<TestPerson4>(actualJson)

        assertThat(actualJson, equalTo(expectedJson))
        assertThat(newPerson, equalTo(expectedPerson))
    }

    data class TestPerson5(val name: String, age: Int) {
        val age: Int = age
    }
    Test fun testDataClass_With_No_Field_Parameters_But_Field_Declared_Inside_initialized_from_parameter() {

        val expectedJson = """{"name":"John Smith","age":30}"""
        val expectedPerson = TestPerson5("John Smith", 30)

        val actualJson = mapper.writeValueAsString(expectedPerson)
        val newPerson  = mapper.readValue<TestPerson5>(actualJson)

        assertThat(actualJson, equalTo(expectedJson))
        assertThat(newPerson, equalTo(expectedPerson))
    }

    class TestPerson6 {
        val name: String
        val age: Int
        constructor(name: String, age: Int) {
           this.name = name
            this.age = age
        }
    }
    Test fun testDataClass_WithOnlySecondaryConstructor() {

        val expectedJson = """{"name":"John Smith","age":30}"""
        val expectedPerson = TestPerson6("John Smith", 30)

        val actualJson = mapper.writeValueAsString(expectedPerson)
        val newPerson  = mapper.readValue<TestPerson6>(actualJson)

        assertThat(actualJson, equalTo(expectedJson))
        assertThat(newPerson.name, equalTo(expectedPerson.name))
        assertThat(newPerson.age, equalTo(expectedPerson.age))
    }


    class TestPerson7(val name: String, val age: Int) {
        constructor(nameAndAge: String) : this(nameAndAge.substringBefore(':'), nameAndAge.substringAfter(':').toInt()) {

        }
    }
    Test fun testDataClass_WithPrimaryAndSecondaryConstructor() {

        val expectedJson = """{"name":"John Smith","age":30}"""
        val expectedPerson = TestPerson7("John Smith", 30)

        val actualJson = mapper.writeValueAsString(expectedPerson)
        val newPerson  = mapper.readValue<TestPerson7>(actualJson)

        assertThat(actualJson, equalTo(expectedJson))
        assertThat(newPerson.name, equalTo(expectedPerson.name))
        assertThat(newPerson.age, equalTo(expectedPerson.age))
    }

    class TestPerson8(name: String) {
        val name: String = name
        var age: Int = 0
        [JsonCreator] constructor(name: String, age: Int) : this(name) {
            println("used?")
           this.age = age
        }
    }

    Test fun testDataClass_WithPrimaryAndSecondaryConstructorBothCouldBeUsedToDeserialize() {

        val expectedJson = """{"name":"John Smith","age":30}"""
        val expectedPerson = TestPerson8("John Smith", 30)

        val actualJson = mapper.writeValueAsString(expectedPerson)
        val newPerson  = mapper.readValue<TestPerson8>(actualJson)

        assertThat(actualJson, equalTo(expectedJson))
        assertThat(newPerson.name, equalTo(expectedPerson.name))
        assertThat(newPerson.age, equalTo(expectedPerson.age))
    }


}