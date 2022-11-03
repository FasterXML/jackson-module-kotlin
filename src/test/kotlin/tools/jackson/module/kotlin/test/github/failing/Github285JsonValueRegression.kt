package tools.jackson.module.kotlin.test.github.failing

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals

class Github297JsonValueRegression {
    data class StringValue297(@get:JsonIgnore val s: String) {
        @JsonValue override fun toString() = s
    }

    @Test fun testJsonValueDataClassIgnored297() {
        val expectedJson = "\"test\""
        val expectedObj = StringValue297("test")

        val actualJson = jacksonObjectMapper().writeValueAsString(expectedObj)
        assertEquals(expectedJson, actualJson)

        val actualObj = jacksonObjectMapper().readValue<StringValue297>("\"test\"")
        assertEquals(expectedObj, actualObj)

    }
}
