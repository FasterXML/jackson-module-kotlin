package tools.jackson.module.kotlin.test.github.failing

import com.fasterxml.jackson.annotation.JsonProperty
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

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
        val dataClassInstance = mapper.readValue<TestClass>(jsonData)
        assertEquals(50000.toUShort(), dataClassInstance.id)
    }
}
