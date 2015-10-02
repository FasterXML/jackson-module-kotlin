package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import kotlin.test.assertEquals

public class TestGithub15 {
    @Test public fun testEnumConstructorWithParm() {
        val one = jacksonObjectMapper().readValue("\"ONE\"", TestEnum::class.java)
        assertEquals(TestEnum.ONE, one)
        val two = jacksonObjectMapper().readValue("\"TWO\"", TestEnum::class.java)
        assertEquals(TestEnum.TWO, two)
    }

    @Test public fun testNormEnumWithoutParam() {
        val one = jacksonObjectMapper().readValue("\"ONE\"", TestOther::class.java)
        assertEquals(TestOther.ONE, one)
        val two = jacksonObjectMapper().readValue("\"TWO\"", TestOther::class.java)
        assertEquals(TestOther.TWO, two)
    }
}

private enum class TestEnum(val i: Int) {
    ONE(1),
    TWO(2)
}

private enum class TestOther {
    ONE, TWO
}
