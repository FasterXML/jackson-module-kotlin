package tools.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonTypeInfo
import tools.jackson.module.kotlin.readValue
import tools.jackson.module.kotlin.jsonMapper
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "_type")
private sealed class BaseClass

private data class ChildClass(val text: String) : BaseClass()

class GitHub844 {
    @Test
    fun test() {
        val json = """
        {
            "_type": "ChildClass",
            "text": "Test"
        }
        """

        val jacksonObjectMapper = jsonMapper()
        val message = jacksonObjectMapper.readValue<BaseClass>(json)

        assertEquals(ChildClass("Test"), message)
    }
}
