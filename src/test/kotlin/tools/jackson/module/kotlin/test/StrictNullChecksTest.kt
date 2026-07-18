package tools.jackson.module.kotlin.test

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tools.jackson.databind.exc.InvalidNullException
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinFeature
import tools.jackson.module.kotlin.kotlinModule
import tools.jackson.module.kotlin.readValue
import kotlin.test.assertNull

class StrictNullChecksTest {
    private val mapper = JsonMapper.builder()
            .addModule(kotlinModule { enable(KotlinFeature.StrictNullChecks) })
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
        assertThrows<InvalidNullException> {
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
        assertThrows<InvalidNullException> {
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
        assertThrows<InvalidNullException> {
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
        assertThrows<InvalidNullException> {
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
        assertThrows<InvalidNullException> {
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
        assertThrows<InvalidNullException> {
            val json = """{"samples":[1, null]}"""
            mapper.readValue<TestClass<Array<Int>>>(json)
        }
    }

    /** use-site projection tests */

    private data class ClassWithInProjectionArray(val samples: Array<in String>)

    @Test
    fun testInProjectionArray() {
        // Null contents for an in projection are allowed regardless of the nullability
        // of the type argument because Array<in String> may actually be Array<Any?>.
        val json = """{"samples":["str", null]}"""
        val stateObj = mapper.readValue<ClassWithInProjectionArray>(json)
        assertEquals(listOf("str", null), stateObj.samples.toList())
    }

    private data class ClassWithNullableInProjectionArray(val samples: Array<in String?>)

    @Test
    fun testNullableInProjectionArray() {
        // If the projected type is marked nullable, the check is not applied.
        val json = """{"samples":["str", null]}"""
        val stateObj = mapper.readValue<ClassWithNullableInProjectionArray>(json)
        assertEquals(listOf("str", null), stateObj.samples.toList())
    }

    private data class ClassWithInProjectionList(val samples: MutableList<in String>)

    @Test
    fun testInProjectionList() {
        val json = """{"samples":["str", null]}"""
        val stateObj = mapper.readValue<ClassWithInProjectionList>(json)
        assertEquals(listOf("str", null), stateObj.samples.toList())
    }

    private data class ClassWithInProjectionMap(val samples: MutableMap<String, in Int>)

    @Test
    fun testInProjectionMapValue() {
        val json = """{ "samples": { "key": null } }"""
        val stateObj = mapper.readValue<ClassWithInProjectionMap>(json)
        assertEquals(mapOf<String, Int?>("key" to null), stateObj.samples.toMap())
    }
}
