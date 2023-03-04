package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.testPrettyWriter
import kotlin.test.assertEquals
import org.junit.Test

class GitHub530 {
    // At the moment, the output is the same with or without `JsonValue`,
    // but this pattern is included in the test case in case the option to default to a serialization method that
    // does not `unbox` is introduced in the future.
    @JvmInline
    value class ValueParamGetterAnnotated(@get:JsonValue val value: Int)

    @JvmInline
    value class ValueParamFieldAnnotated(@JvmField @field:JsonValue val value: Int)

    @JvmInline
    value class PropertyWithOverriddenGetter(val value: Int) {
        @get:JsonValue
        val jsonValue: String
            get() = this.toString()
    }

    @JvmInline
    value class DirectlyOverriddenGetter(val value: Int) {
        @JsonValue
        fun getJsonValue(): String = this.toString()
    }

    interface JsonValueGetter {
        @get:JsonValue
        val jsonValue: String
            get() = this.toString()
    }

    @JvmInline
    value class JsonValueGetterImplementation(val value: Int) : JsonValueGetter

    private val writer = jacksonMapperBuilder().build().testPrettyWriter()

    @Test
    fun valueParamGetterAnnotated() {
        data class Data(
            val nonNull: ValueParamGetterAnnotated,
            val nullable: ValueParamGetterAnnotated?
        )

        assertEquals(
            """
                {
                  "nonNull" : 0,
                  "nullable" : 1
                }
            """.trimIndent(),
            writer.writeValueAsString(
                Data(
                    ValueParamGetterAnnotated(0),
                    ValueParamGetterAnnotated(1)
                )
            )
        )
    }

    @Test
    fun valueParamFieldAnnoated() {
        data class Data(
            val nonNull: ValueParamFieldAnnotated,
            val nullable: ValueParamFieldAnnotated?
        )

        assertEquals(
            """
                {
                  "nonNull" : 0,
                  "nullable" : 1
                }
            """.trimIndent(),
            writer.writeValueAsString(
                Data(
                    ValueParamFieldAnnotated(0),
                    ValueParamFieldAnnotated(1)
                )
            )
        )
    }

    @Test
    fun propertyWithOverriddenGetter() {
        data class Data(
            val nonNull: PropertyWithOverriddenGetter,
            val nullable: PropertyWithOverriddenGetter?
        )

        assertEquals(
            """
                {
                  "nonNull" : "PropertyWithOverriddenGetter(value=0)",
                  "nullable" : "PropertyWithOverriddenGetter(value=1)"
                }
            """.trimIndent(),
            writer.writeValueAsString(
                Data(
                    PropertyWithOverriddenGetter(0),
                    PropertyWithOverriddenGetter(1)
                )
            )
        )
    }

    @Test
    fun directlyOverriddenGetter() {
        data class Data(
            val nonNull: DirectlyOverriddenGetter,
            val nullable: DirectlyOverriddenGetter?
        )

        assertEquals(
            """
                {
                  "nonNull" : "DirectlyOverriddenGetter(value=0)",
                  "nullable" : "DirectlyOverriddenGetter(value=1)"
                }
            """.trimIndent(),
            writer.writeValueAsString(
                Data(
                    DirectlyOverriddenGetter(0),
                    DirectlyOverriddenGetter(1)
                )
            )
        )
    }

    @Test
    fun propertyWithOverriddenGetterAsParameterizedType() {
        data class Data<T : Any>(
            val nonNull: T,
            val nullable: T?
        )

        assertEquals(
            """
                {
                  "nonNull" : "PropertyWithOverriddenGetter(value=0)",
                  "nullable" : "PropertyWithOverriddenGetter(value=1)"
                }
            """.trimIndent(),
            writer.writeValueAsString(
                Data(
                    PropertyWithOverriddenGetter(0),
                    PropertyWithOverriddenGetter(1)
                )
            )
        )
    }

    @Test
    fun jsonValueGetterImplementationAsConcreteType() {
        data class Data(
            val nonNull: JsonValueGetterImplementation,
            val nullable: JsonValueGetterImplementation?
        )

        assertEquals(
            """
                {
                  "nonNull" : "JsonValueGetterImplementation(value=0)",
                  "nullable" : "JsonValueGetterImplementation(value=1)"
                }
            """.trimIndent(),
            writer.writeValueAsString(
                Data(
                    JsonValueGetterImplementation(0),
                    JsonValueGetterImplementation(1)
                )
            )
        )
    }

    @Test
    fun jsonValueGetterImplementationAsGenericType() {
        data class Data(
            val nonNull: JsonValueGetter,
            val nullable: JsonValueGetter?
        )

        assertEquals(
            """
                {
                  "nonNull" : "JsonValueGetterImplementation(value=0)",
                  "nullable" : "JsonValueGetterImplementation(value=1)"
                }
            """.trimIndent(),
            writer.writeValueAsString(
                Data(
                    JsonValueGetterImplementation(0),
                    JsonValueGetterImplementation(1)
                )
            )
        )
    }

    @Test
    fun inCollection() {
        assertEquals(
            "[ 0, 1, \"PropertyWithOverriddenGetter(value=2)\", \"DirectlyOverriddenGetter(value=3)\", \"JsonValueGetterImplementation(value=4)\" ]",
            writer.writeValueAsString(
                listOf(
                    ValueParamGetterAnnotated(0),
                    ValueParamFieldAnnotated(1),
                    PropertyWithOverriddenGetter(2),
                    DirectlyOverriddenGetter(3),
                    JsonValueGetterImplementation(4)
                )
            )
        )
    }

    @Test
    fun inArray() {
        assertEquals(
            "[ 0, 1, \"PropertyWithOverriddenGetter(value=2)\", \"DirectlyOverriddenGetter(value=3)\", \"JsonValueGetterImplementation(value=4)\" ]",
            writer.writeValueAsString(
                arrayOf(
                    ValueParamGetterAnnotated(0),
                    ValueParamFieldAnnotated(1),
                    PropertyWithOverriddenGetter(2),
                    DirectlyOverriddenGetter(3),
                    JsonValueGetterImplementation(4)
                )
            )
        )
    }
}
