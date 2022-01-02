package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import org.junit.Test
import kotlin.test.assertEquals

class GitHub530 {
    // At the moment, the output is the same with or without `JsonValue`,
    // but this pattern is included in the test case in case the option to default to a serialization method that
    // does not `unbox` is introduced in the future.
    @JvmInline value class Foo(@get:JsonValue val value: Int)
    @JvmInline value class Bar(@JvmField @field:JsonValue val value: Int)

    @JvmInline value class Baz(val value: Int) {
        @get:JsonValue val jsonValue: String get() = this.toString()
    }
    @JvmInline value class Qux(val value: Int) {
        @JsonValue fun getJsonValue(): String = this.toString()
    }

    interface JsonValueGetter { @get:JsonValue val jsonValue: String get() = this.toString() }
    @JvmInline value class Quux(val value: Int): JsonValueGetter
    @JvmInline value class Corge(val value: Int): JsonValueGetter

    @JvmInline value class Grault(val value: Int) {
        @get:JsonValue val jsonValue: String get() = this.toString()
    }

    data class Data<T : Any>(
        val foo1: Foo,
        val foo2: Foo?,
        val bar1: Bar,
        val bar2: Bar?,
        val baz1: Baz,
        val baz2: Baz?,
        val qux1: Qux,
        val qux2: Qux?,
        val quux1: Quux,
        val quux2: Quux?,
        val corge1: JsonValueGetter,
        val corge2: JsonValueGetter?,
        val grault1: T,
        val grault2: T?
    )

    @Test
    fun test() {
        val writer = jacksonMapperBuilder().build().writerWithDefaultPrettyPrinter()

        assertEquals(
            """
                {
                  "foo1" : 0,
                  "foo2" : 1,
                  "bar1" : 2,
                  "bar2" : 3,
                  "baz1" : "Baz(value=4)",
                  "baz2" : "Baz(value=5)",
                  "qux1" : "Qux(value=6)",
                  "qux2" : "Qux(value=7)",
                  "quux1" : "Quux(value=8)",
                  "quux2" : "Quux(value=9)",
                  "corge1" : "Corge(value=10)",
                  "corge2" : "Corge(value=11)",
                  "grault1" : "Grault(value=12)",
                  "grault2" : "Grault(value=13)",
                }
            """.trimIndent(),
            writer.writeValueAsString(
                Data(
                    Foo(0), Foo(1),
                    Bar(2), Bar(3),
                    Baz(4), Baz(5),
                    Qux(6), Qux(7),
                    Quux(8), Quux(9),
                    Corge(10), Corge(11),
                    Grault(12), Grault(13)
                )
            )
        )
    }
}
