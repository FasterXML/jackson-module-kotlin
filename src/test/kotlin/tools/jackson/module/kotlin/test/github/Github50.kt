package tools.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonUnwrapped
import tools.jackson.databind.exc.InvalidDefinitionException
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import tools.jackson.module.kotlin.test.expectFailure
import org.junit.Test
import kotlin.test.assertEquals

class TestGithub50 {
    data class Name(val firstName: String, val lastName: String)

    data class Employee(
            @get:JsonUnwrapped val name: Name,
            val position: String
    )

    @Test
    fun testGithub50UnwrappedError() {
        val json = """{"firstName":"John","lastName":"Smith","position":"Manager"}"""
        val obj: Employee = jacksonObjectMapper().readValue(json)
        assertEquals(Name("John", "Smith"), obj.name)
        assertEquals("Manager", obj.position)
    }
}
