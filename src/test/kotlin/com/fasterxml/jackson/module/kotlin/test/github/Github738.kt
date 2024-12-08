package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class Github738 {
    data class D(@JsonSetter(nulls = Nulls.FAIL) val v: Int)

    @Test
    fun test() {
        val mapper = jacksonObjectMapper()
        // nulls = FAIL is reflected if it is primitive and missing
        assertThrows(MismatchedInputException::class.java) { mapper.readValue<D>("{}") }
    }
}
