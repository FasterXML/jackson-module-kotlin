package tools.jackson.module.kotlin.test.github

import tools.jackson.annotation.JsonCreator
import tools.jackson.annotation.JsonValue
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import org.junit.Assert.assertEquals
import org.junit.Test

class TestGithub91 {
    data class DataClass1(val name: String, val content: DataClass2)

    data class DataClass2 @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(@JsonValue val content: String)

    private val jsonData = """
        {
            "name": "my name",
            "content": "some value"
        }
        """

    @Test
    fun testJsonParsing() {
        val mapper = _root_ide_package_.tools.jackson.module.kotlin.jacksonObjectMapper()
        val dataClass1 = mapper.readValue<DataClass1>(jsonData)
        assertEquals(DataClass1("my name", DataClass2("some value")), dataClass1)
        assertEquals("{\"name\":\"my name\",\"content\":\"some value\"}", mapper.writeValueAsString(dataClass1))
    }
}
