package tools.jackson.module.kotlin.test.github

import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertSame

/**
 * An empty object should be deserialized as *the* Unit instance
 */
class TestGithub196 {
    @Test
    fun testUnitSingletonDeserialization() {
        assertSame(jacksonObjectMapper().readValue("{}"), Unit)
    }
}
