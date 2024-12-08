package com.fasterxml.jackson.module.kotlin.kogeraIntegration.deser.valueClass.jsonCreator

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

// Test on the case of deserialization by ValueClassBoxDeserializer
class HandledByKogeraTest {
    @JvmInline
    value class SpecifiedPrimary @JsonCreator constructor(val primary: String?)

    @JvmInline
    value class Secondary(val value: String) {
        @JsonCreator constructor(value: Int) : this("$value-creator")
    }

    @JvmInline
    value class Factory(val value: Int) {
        companion object {
            @JvmStatic
            @JsonCreator
            fun creator(value: Int): Factory = Factory(value + 100)
        }
    }

    @Test
    fun directDeserTest() {
        val mapper = jacksonObjectMapper()

        assertEquals(SpecifiedPrimary("b"), mapper.readValue<SpecifiedPrimary>("\"b\""))
        assertEquals(Secondary("1-creator"), mapper.readValue<Secondary>("1"))
        assertEquals(Factory(101), mapper.readValue<Factory>("1"))
    }

    data class Dst(
        val bar: SpecifiedPrimary,
        val baz: Secondary,
        val qux: Factory
    )

    @Test
    fun parameterTest() {
        val mapper = jacksonObjectMapper()

        val r = mapper.readValue<Dst>(
            """
            {
              "bar":"b",
              "baz":1,
              "qux":1
            }
            """.trimIndent()
        )

        assertEquals(
            Dst(
                SpecifiedPrimary("b"),
                Secondary("1-creator"),
                Factory(101)
            ),
            r
        )
    }

    @JvmInline
    value class MultipleValueConstructor(val value: String) {
        @JsonCreator constructor(v1: String, v2: String) : this(v1 + v2)
    }

    @JvmInline
    value class MultipleValueFactory(val value: Int) {
        companion object {
            @JsonCreator
            @JvmStatic
            fun creator(v1: Int, v2: Int): MultipleValueFactory = MultipleValueFactory(v1 + v2)
        }
    }

    // A Creator that requires multiple arguments is basically an error.
    @Test
    fun handleErrorTest() {
        val mapper = jacksonObjectMapper()

        assertThrows(InvalidDefinitionException::class.java) {
            mapper.readValue<MultipleValueConstructor>("""{"v1":"1","v2":"2"}""")
        }
        assertThrows(InvalidDefinitionException::class.java) {
            mapper.readValue<MultipleValueFactory>("""{"v1":1,"v2":2}""")
        }
    }
}
