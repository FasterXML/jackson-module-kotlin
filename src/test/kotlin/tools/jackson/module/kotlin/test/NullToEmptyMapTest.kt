package tools.jackson.module.kotlin.test

import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinFeature.NullToEmptyMap
import tools.jackson.module.kotlin.kotlinModule
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestNullToEmptyMap {

    private data class TestClass(val foo: Map<String, Int>)

    @Test
    fun nonNullCaseStillWorks() {
        val mapper = createMapper()
        assertEquals(mapOf("bar" to 1), mapper.readValue("""{"foo": {"bar": 1}}""", TestClass::class.java).foo)
    }

    @Test
    fun shouldMapNullValuesToEmpty() {
        val mapper = createMapper()
        assertEquals(emptyMap(), mapper.readValue("{}", TestClass::class.java).foo)
        assertEquals(emptyMap(), mapper.readValue("""{"foo": null}""", TestClass::class.java).foo)

    }

    private fun createMapper(): ObjectMapper {
        return JsonMapper.builder()
                .addModule(kotlinModule { enable(NullToEmptyMap) })
                .build()
    }
}
