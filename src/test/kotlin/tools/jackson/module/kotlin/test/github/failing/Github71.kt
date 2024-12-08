package tools.jackson.module.kotlin.test.github.failing

import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import tools.jackson.module.kotlin.test.expectFailure
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestGithub71 {
    open class Identifiable {
        internal var identity: Long? = null
    }

    @Test
    fun testInternalPropertySerliazation() {
        val json = jacksonObjectMapper().writeValueAsString(Identifiable())

        expectFailure<AssertionError>("GitHub #71 has been fixed!") {
            assertEquals("{\"identity\":null}", json) // fails: {"identity$jackson_module_kotlin":null}
            val newInstance = jacksonObjectMapper().readValue<Identifiable>(json)
            assertEquals(Identifiable(), newInstance)
        }
    }
}
