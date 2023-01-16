package com.fasterxml.jackson.module.kotlin.test.github.failing

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.test.expectFailure
import org.junit.Test

class TestGithub611 {

    class TestClass(@JsonProperty("id") var id: UShort) {
        // Empty constructor
        constructor() : this(1u)
    }

    // Value fits into UShort, but not (Java) Short
    private val jsonData = """
        {
            "id": 50000
        }
        """

    @Test
    fun testJsonParsing() {
        val mapper = jacksonObjectMapper()
        expectFailure<JsonMappingException>("GitHub #611 has been fixed!") {
            val dataClassInstance = mapper.readValue<TestClass>(jsonData)
        }
    }
}
