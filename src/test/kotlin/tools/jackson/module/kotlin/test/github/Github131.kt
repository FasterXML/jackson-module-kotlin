package tools.jackson.module.kotlin.test.github

import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals

class TestGithub131 {
    open class BaseClass(val name: String)

    class DerivedClass(name: String) : BaseClass(name)

    @Test
    fun testFailureCase() {
        val mapper = jacksonObjectMapper()
        val x = mapper.readValue<DerivedClass>("""{"name":"abc"}""")
        assertEquals(DerivedClass("abc").name, x.name)
    }
}