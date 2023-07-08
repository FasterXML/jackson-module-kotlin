package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.KotlinFeature.StrictNullChecks
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertNull

class StrictNullChecksTest {
    private val mapper = ObjectMapper().registerModule(kotlinModule { enable(StrictNullChecks) })

    /** collection tests */

    private data class ClassWithListOfNullableInt(val samples: List<Int?>)

    @Test
    fun testListOfNullableInt() {
        val json = """{"samples":[1, null]}"""
        val stateObj = mapper.readValue<ClassWithListOfNullableInt>(json)
        assertThat(stateObj.samples, equalTo(listOf(1, null)))
    }

    private data class ClassWithListOfInt(val samples: List<Int>)

    @Test(expected = MismatchedInputException::class)
    fun testListOfInt() {
        val json = """{"samples":[1, null]}"""
        mapper.readValue<ClassWithListOfInt>(json)
    }

    private data class ClassWithNullableListOfInt(val samples: List<Int>?)

    @Test
    fun testNullableListOfInt() {
        val json = """{"samples": null}"""
        val stateObj = mapper.readValue<ClassWithNullableListOfInt>(json)
        assertNull(stateObj.samples)
    }

    /** array tests */

    private data class ClassWithArrayOfNullableInt(val samples: Array<Int?>)

    @Test
    fun testArrayOfNullableInt() {
        val json = """{"samples":[1, null]}"""
        val stateObj = mapper.readValue<ClassWithArrayOfNullableInt>(json)
        assertThat(stateObj.samples, equalTo(arrayOf(1, null)))
    }

    private data class ClassWithArrayOfInt(val samples: Array<Int>)

    @Test(expected = MismatchedInputException::class)
    fun testArrayOfInt() {
        val json = """{"samples":[1, null]}"""
        mapper.readValue<ClassWithArrayOfInt>(json)
    }

    private data class ClassWithNullableArrayOfInt(val samples: Array<Int>?)

    @Test
    fun testNullableArrayOfInt() {
        val json = """{"samples": null}"""
        val stateObj = mapper.readValue<ClassWithNullableArrayOfInt>(json)
        assertNull(stateObj.samples)
    }

    /** map tests */

    private data class ClassWithMapOfStringToNullableInt(val samples: Map<String, Int?>)

    @Test
    fun testMapOfStringToNullableInt() {
        val json = """{ "samples": { "key": null } }"""
        val stateObj = mapper.readValue<ClassWithMapOfStringToNullableInt>(json)
        assertThat(stateObj.samples, equalTo(mapOf<String, Int?>("key" to null)))
    }

    private data class ClassWithMapOfStringToInt(val samples: Map<String, Int>)

    @Test(expected = MismatchedInputException::class)
    fun testMapOfStringToIntWithNullValue() {
        val json = """{ "samples": { "key": null } }"""
        mapper.readValue<ClassWithMapOfStringToInt>(json)
    }

    private data class ClassWithNullableMapOfStringToInt(val samples: Map<String, Int>?)

    @Test
    fun testNullableMapOfStringToInt() {
        val json = """{"samples": null}"""
        val stateObj = mapper.readValue<ClassWithNullableMapOfStringToInt>(json)
        assertNull(stateObj.samples)
    }

    /** generics test */

    private data class TestClass<T>(val samples: T)

    @Test
    fun testListOfGeneric() {
        val json = """{"samples":[1, 2]}"""
        val stateObj = mapper.readValue<TestClass<List<Int>>>(json)
        assertThat(stateObj.samples, equalTo(listOf(1, 2)))
    }

    @Ignore // this is a hard problem to solve and is currently not addressed
    @Test(expected = MismatchedInputException::class)
    fun testListOfGenericWithNullValue() {
        val json = """{"samples":[1, null]}"""
        mapper.readValue<TestClass<List<Int>>>(json)
    }

    @Test
    fun testMapOfGeneric() {
        val json = """{ "samples": { "key": 1 } }"""
        val stateObj = mapper.readValue<TestClass<Map<String, Int>>>(json)
        assertThat(stateObj.samples, equalTo(mapOf("key" to 1)))
    }

    @Ignore // this is a hard problem to solve and is currently not addressed
    @Test(expected = MismatchedInputException::class)
    fun testMapOfGenericWithNullValue() {
        val json = """{ "samples": { "key": null } }"""
        mapper.readValue<TestClass<Map<String, Int>>>(json)
    }

    @Test
    fun testArrayOfGeneric() {
        val json = """{"samples":[1, 2]}"""
        val stateObj = mapper.readValue<TestClass<Array<Int>>>(json)
        assertThat(stateObj.samples, equalTo(arrayOf(1, 2)))
    }

    @Ignore // this is a hard problem to solve and is currently not addressed
    @Test(expected = MismatchedInputException::class)
    fun testArrayOfGenericWithNullValue() {
        val json = """{"samples":[1, null]}"""
        mapper.readValue<TestClass<Array<Int>>>(json)
    }
}
