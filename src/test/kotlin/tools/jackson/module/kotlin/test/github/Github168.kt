package tools.jackson.module.kotlin.test.github

import tools.jackson.annotation.JsonProperty
import tools.jackson.module.kotlin.MissingKotlinParameterException
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals

class TestGithub168 {
    @Suppress("UNUSED_PARAMETER")
    class TestClass(@JsonProperty(value = "foo", required = true) foo: String?, val baz: String)

    @Test
    fun testIfRequiredIsReallyRequiredWhenNullused() {
        val obj = _root_ide_package_.tools.jackson.module.kotlin.jacksonObjectMapper().readValue<TestClass>("""{"foo":null,"baz":"whatever"}""")
        assertEquals("whatever", obj.baz)
    }

    @Test(expected = tools.jackson.module.kotlin.MissingKotlinParameterException::class)
    fun testIfRequiredIsReallyRequiredWhenAbsent() {
        val obj = _root_ide_package_.tools.jackson.module.kotlin.jacksonObjectMapper().readValue<TestClass>("""{"baz":"whatever"}""")
        assertEquals("whatever", obj.baz)
    }

    @Test
    fun testIfRequiredIsReallyRequiredWhenVauePresent() {
        val obj = _root_ide_package_.tools.jackson.module.kotlin.jacksonObjectMapper().readValue<TestClass>("""{"foo":"yay!","baz":"whatever"}""")
        assertEquals("whatever", obj.baz)
    }
}
