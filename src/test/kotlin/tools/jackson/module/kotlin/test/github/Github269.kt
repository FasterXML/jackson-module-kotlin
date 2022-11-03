package tools.jackson.module.kotlin.test.github

import tools.jackson.annotation.JsonCreator
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals

class TestGithub269 {
    data class Foo(val pattern: Regex)
    data class Bar(val thing: Regex)

    data class Goo(
            @JsonSerialize(using = ToStringSerializer::class)
            val myPattern: Regex
    ) {
        constructor(strPattern: String) : this(Regex(strPattern))
    }

    data class Zoo(
            @JsonSerialize(using = ToStringSerializer::class)
            val myPattern: Regex
    ) {
        @JsonCreator
        constructor(strPattern: String) : this(Regex(strPattern))
    }

    @Test
    fun testGithub269WithFoo() {
        val mapper = _root_ide_package_.tools.jackson.module.kotlin.jacksonObjectMapper()

        val testObject = Foo(Regex("test"))
        val testJson = mapper.writeValueAsString(testObject)
        val resultObject = mapper.readValue<Foo>(testJson)

        assertEquals(testObject.pattern.pattern, resultObject.pattern.pattern)
        assertEquals(testObject.pattern.options, resultObject.pattern.options)

        mapper.readValue<Foo>("""{"pattern":"test"}""")
    }

    @Test
    fun testGithub269WithBar() {
        val mapper = _root_ide_package_.tools.jackson.module.kotlin.jacksonObjectMapper()

        val testObject = Bar(Regex("test"))
        val testJson = mapper.writeValueAsString(testObject)
        val resultObject = mapper.readValue<Bar>(testJson)

        assertEquals(testObject.thing.pattern, resultObject.thing.pattern)
        assertEquals(testObject.thing.options, resultObject.thing.options)

        mapper.readValue<Bar>("""{"thing":"test"}""")
    }

    @Test
    fun testGithub269WithGoo() {
        val mapper = _root_ide_package_.tools.jackson.module.kotlin.jacksonObjectMapper()

        val testObject = Goo(Regex("test_pattern_1"))
        val testJson = mapper.writeValueAsString(testObject)
        val resultObject = mapper.readValue<Goo>(testJson)

        assertEquals(testObject.myPattern.pattern, resultObject.myPattern.pattern)
    }

    @Test
    fun testGithub269WithZoo() {
        val mapper = _root_ide_package_.tools.jackson.module.kotlin.jacksonObjectMapper()

        val testObject = Zoo(Regex("test_pattern_1"))
        val testJson = mapper.writeValueAsString(testObject)
        val resultObject = mapper.readValue<Zoo>(testJson)

        assertEquals(testObject.myPattern.pattern, resultObject.myPattern.pattern)
    }
}