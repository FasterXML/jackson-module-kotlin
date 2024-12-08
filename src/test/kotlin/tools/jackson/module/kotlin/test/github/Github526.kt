package tools.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Github526 {
    data class D(@JsonSetter(nulls = Nulls.SKIP) val v: Int = -1)

    @Test
    fun test() {
        val mapper = jacksonObjectMapper()
        val d = mapper.readValue<D>("""{"v":null}""")

        assertEquals(-1, d.v)
    }
}
