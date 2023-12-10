package tools.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import org.junit.Assert.assertThrows
import org.junit.Test
import tools.jackson.databind.exc.MismatchedInputException
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue

class Github738 {
    data class D(@JsonSetter(nulls = Nulls.FAIL) val v: Int)

    @Test
    fun test() {
        val mapper = jacksonObjectMapper()
        // nulls = FAIL is reflected if it is primitive and missing
        assertThrows(MismatchedInputException::class.java) { mapper.readValue<D>("{}") }
    }
}
