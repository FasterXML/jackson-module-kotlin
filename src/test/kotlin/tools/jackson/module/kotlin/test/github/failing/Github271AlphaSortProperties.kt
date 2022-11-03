package tools.jackson.module.kotlin.test.github.failing

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import tools.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.test.expectFailure
import org.junit.Test
import kotlin.test.assertEquals

class TestGithub271 {
    @JsonPropertyOrder(alphabetic=true)
    data class Foo(val a: String, val c: String) {
        val b = "b"
    }

    @Test
    fun testAlphabeticFields() {
        val mapper = _root_ide_package_.tools.jackson.module.kotlin.jacksonObjectMapper()

        val json = mapper.writeValueAsString(Foo("a", "c"))
        expectFailure<AssertionError>("GitHub #271 has been fixed!") {
            assertEquals("""{"a":"a","b":"b","c":"c"}""", json)
        }
    }
}
