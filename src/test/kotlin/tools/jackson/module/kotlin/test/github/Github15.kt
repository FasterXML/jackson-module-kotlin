package tools.jackson.module.kotlin.test.github

import tools.jackson.module.kotlin.*
import org.junit.Test
import kotlin.test.assertEquals

class TestGithub15 {
    @Test fun testEnumConstructorWithParm() {
        val one = _root_ide_package_.tools.jackson.module.kotlin.jacksonObjectMapper().readValue("\"ONE\"", TestEnum::class.java)
        assertEquals(TestEnum.ONE, one)
        val two = _root_ide_package_.tools.jackson.module.kotlin.jacksonObjectMapper().readValue("\"TWO\"", TestEnum::class.java)
        assertEquals(TestEnum.TWO, two)
    }

    @Test fun testNormEnumWithoutParam() {
        val one = _root_ide_package_.tools.jackson.module.kotlin.jacksonObjectMapper().readValue("\"ONE\"", TestOther::class.java)
        assertEquals(TestOther.ONE, one)
        val two = _root_ide_package_.tools.jackson.module.kotlin.jacksonObjectMapper().readValue("\"TWO\"", TestOther::class.java)
        assertEquals(TestOther.TWO, two)
    }

    @Test fun testClassWithEnumsNeedingConstruction() {
        val obj: UsingEnum = _root_ide_package_.tools.jackson.module.kotlin.jacksonObjectMapper().readValue("""{"x":"ONE","y":"TWO"}""")
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
