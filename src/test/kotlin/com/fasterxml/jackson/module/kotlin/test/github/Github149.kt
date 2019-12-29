package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test

class TestGithub149 {

    class Foo(val name: String, attributes: List<FooAtt>) {
        @JsonManagedReference
        private val _attributes: MutableList<FooAtt> = attributes.toMutableList()

        val attributes: List<FooAtt>
            @JsonProperty("attributes")
            get() = _attributes

        override fun toString(): String {
            return "Foo(name='$name', _attributes=$_attributes)"
        }
    }

    class FooAtt(val name: String) {
        @JsonBackReference
        lateinit var parent: Foo

        override fun toString(): String {
            return "FooAtt(name='$name', parent=${parent.name})"
        }
    }

    @Test
    fun testDeserializationOfManagedReferences() {
        val mapper = jacksonObjectMapper()
        mapper.setVisibility(
            mapper.serializationConfig.defaultVisibilityChecker
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE)
        )

        val fAtt = FooAtt("f1Att1")
        val f1 = Foo("f1", listOf(fAtt))
        fAtt.parent = f1

//        println(f1)
//        println("=============")

        val f1AsJson = mapper.writeValueAsString(f1)
//        println(f1AsJson)
//        println("=============")
        val mFromJson = mapper.readValue(f1AsJson, Foo::class.java)
//        println(mFromJson)
    }

    data class Car(
        val id: Long,

        @JsonManagedReference
        val colors: MutableList<Color> = mutableListOf()

    )

    data class Color (
        val id: Long,
        val code: String) {

        @JsonBackReference
        lateinit var car: Car
    }

    @Test
    fun testGithub129(){
        val mapper = jacksonObjectMapper()
        val c = Car(id = 100)
        val color = Color(id = 100, code = "#FFFFF").apply { car = c }
        c.colors.add(color)
        val s = mapper.writeValueAsString(c)
        val value = mapper.readValue(s, Car::class.java)
//        print(value)
    }
}