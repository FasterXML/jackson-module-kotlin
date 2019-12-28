package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlinFunction
import kotlin.test.assertEquals

class TestGithub114 {
    data class Foo(val bar: String = "default", val baz: String = "default")

    data class FooWithStaticCreator(val bar: String, val baz: String) {

        companion object {
            val someValue = "someDefaultValue"

            @JvmStatic
            @JsonCreator
            fun createFromJson(bar: String = someValue, baz: String = someValue): FooWithStaticCreator = FooWithStaticCreator(bar, baz)
        }
    }

    @Test
    fun testCompanionObjectCreatorWithDefaultParameters() {
        val mapper = jacksonObjectMapper()

        val foo = mapper.readValue<Foo>("""{"baz": "bazValue"}""")
        println(foo)

        val fooWithStaticCreator = mapper.readValue<FooWithStaticCreator>("""{"baz": "bazValue"}""")
        println(fooWithStaticCreator) // Expect FooWithStaticCreator(bar=default, baz=bazValue), result == MismatchedInputException: Missing required creator property 'bar' (index 0)
        assertEquals(FooWithStaticCreator(FooWithStaticCreator.someValue, "bazValue"), fooWithStaticCreator)
    }

    @Test
    fun otherTestVariation() {
        val mapper = jacksonObjectMapper()
        val testObj = mapper.readValue<Obj>("""{"id":1}""")

        assertEquals("yes", testObj.prop)
    }

    data class Obj(val id: String, val prop: String) {
        companion object {
            @JsonCreator
            @JvmStatic
            fun parse(@JsonProperty("id") id: String,
                      @JsonProperty("name") name: String? = null) = Obj(id, name
                    ?: "yes")
        }
    }

    @Test
    fun testCallByFunctionalityWithCompanionObjects() {
        val v = Nada.Companion::foo
        assertEquals("OK 42", v.callBy(mapOf()))
        val v2 = FooWithStaticCreator.Companion::createFromJson.javaMethod!!.kotlinFunction!!
//        println(v2.callBy(mapOf(v2.parameters.first() to FooWithStaticCreator, v2.parameters.drop(1).first() to "asdf")))
    }

    private class Nada {
        companion object {
            @JvmStatic
            fun foo(x: Int = 42) = "OK $x"
        }
    }
}
