package tools.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import tools.jackson.module.kotlin.testPrettyWriter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class GitHub281 {
    @JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@type")
    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator::class, property = "@id")
    interface Entity

    object NumberEntity : Entity

    data class NumberValue(val value: Int) {
        val entity = NumberEntity
    }

    private val json = """
        [ {
          "value" : 10,
          "entity" : {
            "@type" : ".GitHub281${'$'}NumberEntity",
            "@id" : 1
          }
        }, {
          "value" : 11,
          "entity" : 1
        } ]
        """.trimIndent()

    @Test
    fun `test writing involving type, id and object`() {
        val input = listOf(NumberValue(10), NumberValue(11))

        val output = jacksonObjectMapper()
            .testPrettyWriter()
            .writeValueAsString(input)

        assertEquals(json, output)
    }

    @Test
    fun `test reading involving type, id and object`() {
        val output = jacksonObjectMapper().readValue<List<NumberValue>>(json)

        assertEquals(2, output.size)
        val (a, b) = output
        assertSame(NumberEntity::class.java, a.entity.javaClass)
        assertSame(NumberEntity::class.java, b.entity.javaClass)
        assertEquals(10, a.value)
        assertEquals(11, b.value)
    }
}
