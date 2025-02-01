package tools.jackson.module.kotlin.test

import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinFeature.StrictNullChecks
import tools.jackson.module.kotlin.MissingKotlinParameterException
import tools.jackson.module.kotlin.kotlinModule
import tools.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertNull

class StrictNullChecksTestOld {
    private val mapper = JsonMapper.builder()
        .addModule(kotlinModule { enable(StrictNullChecks) })
        .build()

    /** collection tests */

    private data class ClassWithListOfNullableInt(val samples: List<Int?>)

    @Test
    fun testListOfNullableInt() {
        val json = """{"samples":[1, null]}"""
        val stateObj = mapper.readValue<ClassWithListOfNullableInt>(json)
        assertEquals(listOf(1, null), stateObj.samples)
    }

    private data class ClassWithListOfInt(val samples: List<Int>)

    @Test
    fun testListOfInt() {
        assertThrows<MissingKotlinParameterException> {
            val json = """{"samples":[1, null]}"""
            mapper.readValue<ClassWithListOfInt>(json)
        }
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
        assertArrayEquals(arrayOf(1, null), stateObj.samples)
    }

    private data class ClassWithArrayOfInt(val samples: Array<Int>)

    @Test
    fun testArrayOfInt() {
        assertThrows<MissingKotlinParameterException> {
            val json = """{"samples":[1, null]}"""
            mapper.readValue<ClassWithArrayOfInt>(json)
        }
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
        assertEquals(mapOf<String, Int?>("key" to null), stateObj.samples)
    }

    private data class ClassWithMapOfStringToInt(val samples: Map<String, Int>)

    @Test
    fun testMapOfStringToIntWithNullValue() {
        assertThrows<MissingKotlinParameterException> {
            val json = """{ "samples": { "key": null } }"""
            mapper.readValue<ClassWithMapOfStringToInt>(json)
        }
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
        assertEquals(listOf(1, 2), stateObj.samples)
    }

    @Disabled // this is a hard problem to solve and is currently not addressed
    @Test
    fun testListOfGenericWithNullValue() {
        assertThrows<MissingKotlinParameterException> {
            val json = """{"samples":[1, null]}"""
            mapper.readValue<TestClass<List<Int>>>(json)
        }
    }

    @Test
    fun testMapOfGeneric() {
        val json = """{ "samples": { "key": 1 } }"""
        val stateObj = mapper.readValue<TestClass<Map<String, Int>>>(json)
        assertEquals(mapOf("key" to 1), stateObj.samples)
    }

    @Disabled // this is a hard problem to solve and is currently not addressed
    @Test
    fun testMapOfGenericWithNullValue() {
        assertThrows<MissingKotlinParameterException> {
            val json = """{ "samples": { "key": null } }"""
            mapper.readValue<TestClass<Map<String, Int>>>(json)
        }
    }

    @Test
    fun testArrayOfGeneric() {
        val json = """{"samples":[1, 2]}"""
        val stateObj = mapper.readValue<TestClass<Array<Int>>>(json)
        assertArrayEquals(arrayOf(1, 2), stateObj.samples)
    }

    @Disabled // this is a hard problem to solve and is currently not addressed
    @Test
    fun testArrayOfGenericWithNullValue() {
        assertThrows<MissingKotlinParameterException> {
            val json = """{"samples":[1, null]}"""
            mapper.readValue<TestClass<Array<Int>>>(json)
        }
    }
}
