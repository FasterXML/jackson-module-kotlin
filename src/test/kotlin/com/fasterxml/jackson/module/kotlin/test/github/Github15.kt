package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.module.kotlin.*
import org.junit.Test
import kotlin.test.assertEquals

class TestGithub15 {
    @Test fun testEnumConstructorWithParm() {
        val one = jacksonObjectMapper().readValue("\"ONE\"", TestEnum::class.java)
        assertEquals(TestEnum.ONE, one)
        val two = jacksonObjectMapper().readValue("\"TWO\"", TestEnum::class.java)
        assertEquals(TestEnum.TWO, two)
    }

    @Test fun testNormEnumWithoutParam() {
        val one = jacksonObjectMapper().readValue("\"ONE\"", TestOther::class.java)
        assertEquals(TestOther.ONE, one)
        val two = jacksonObjectMapper().readValue("\"TWO\"", TestOther::class.java)
        assertEquals(TestOther.TWO, two)
    }

    @Test fun testClassWithEnumsNeedingConstruction() {
        val obj: UsingEnum = jacksonObjectMapper().readValue("""{"x":"ONE","y":"TWO"}""")
        assertEquals(TestEnum.ONE, obj.x)
        assertEquals(TestOther.TWO, obj.y)
    }
}

private class UsingEnum(val x: TestEnum, val y: TestOther)

private enum class TestEnum(val i: Int) {
    ONE(1),
    TWO(2)
}

private enum class TestOther {
    ONE, TWO
}
