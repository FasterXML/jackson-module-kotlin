package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.databind.exc.InvalidNullException
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class GitHub976 {
    data class PrimitiveList(val list: List<Int>)

    @Test
    fun strictNullChecks() {
        val om = jacksonObjectMapper {
            enable(KotlinFeature.StrictNullChecks)
        }
        assertThrows<InvalidNullException> {
            om.readValue("""{"list": [""] }""".toByteArray(), PrimitiveList::class.java)
        }
    }

    @Test
    fun newStrictNullChecksRegression() {
        val om = jacksonObjectMapper {
            enable(KotlinFeature.NewStrictNullChecks)
        }
        assertThrows<InvalidNullException> {
            om.readValue("""{"list": [""] }""".toByteArray(), PrimitiveList::class.java)
        }
    }
}
