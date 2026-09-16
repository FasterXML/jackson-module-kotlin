package tools.jackson.module.kotlin.test

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.As
import com.fasterxml.jackson.annotation.JsonValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tools.jackson.databind.DefaultTyping
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import tools.jackson.module.kotlin.defaultMapper
import tools.jackson.module.kotlin.jacksonMapperBuilder
import tools.jackson.module.kotlin.readValue

/**
 * Tests that unsigned integers and value classes handled by the serializers/deserializers of this module
 * can be written and read back with polymorphic type information (default typing or `@JsonTypeInfo`).
 *
 * Since these types are all `final`, type information is only applied to them when they are stored in
 * a position whose declared type requires it, e.g. an `Any` property or the contents of an annotated collection.
 * As for other scalar values, the type id is that of the value class itself and the unboxed value is written
 * as the scalar value, e.g. `["pkg.ValueClass", "unboxed"]` for `As.PROPERTY` / `As.WRAPPER_ARRAY`.
 */
class PolymorphicTypeInfoTest {
    @JvmInline
    value class IntVc(val v: Int)

    @JvmInline
    value class StringVc(val v: String)

    @JvmInline
    value class NullableStringVc(val v: String?)

    @JvmInline
    value class JsonValueVc(val v: Int) {
        @JsonValue
        fun jsonValue() = v + 100

        companion object {
            @JvmStatic
            @JsonCreator
            fun fromJsonValue(jsonValue: Int) = JsonValueVc(jsonValue - 100)
        }
    }

    data class Pojo(val x: Int)

    @JvmInline
    value class PojoVc(val v: Pojo)

    data class AnyHolder(val a: Any)

    data class AnyListHolder(val l: List<Any>)

    data class AnyMapHolder(val m: Map<String, Any>)

    // Values handled by this module paired with the JSON of their unboxed representation.
    private val cases: List<Pair<Any, String>> = listOf(
        1.toUByte() to "1",
        1.toUShort() to "1",
        1u to "1",
        ULong.MAX_VALUE to "18446744073709551615",
        IntVc(1) to "1",
        StringVc("s") to "\"s\"",
        NullableStringVc("s") to "\"s\"",
        NullableStringVc(null) to "null",
        JsonValueVc(1) to "101",
        PojoVc(Pojo(1)) to """{"x":1}""",
    )

    private fun typed(inclusion: As, value: Any, unboxedJson: String): String {
        val id = value::class.java.name

        return when (inclusion) {
            As.PROPERTY, As.WRAPPER_ARRAY -> """["$id",$unboxedJson]"""
            As.WRAPPER_OBJECT -> """{"$id":$unboxedJson}"""
            else -> throw IllegalArgumentException("Unsupported inclusion: $inclusion")
        }
    }

    private val typeValidator = BasicPolymorphicTypeValidator.builder().allowIfBaseType(Any::class.java).build()

    private inline fun <reified T : Any> assertRoundTrip(mapper: ObjectMapper, value: T, expectedJson: String) {
        val json = mapper.writeValueAsString(value)
        assertEquals(expectedJson, json)
        assertEquals(value, mapper.readValue<T>(json))
    }

    @Nested
    inner class DefaultTypingTest {
        private fun mapper(inclusion: As): ObjectMapper = jacksonMapperBuilder()
            .activateDefaultTyping(typeValidator, DefaultTyping.OBJECT_AND_NON_CONCRETE, inclusion)
            .build()

        private fun assertAnyHolderRoundTrip(inclusion: As) {
            val mapper = mapper(inclusion)

            cases.forEach { (value, unboxedJson) ->
                assertRoundTrip(mapper, AnyHolder(value), """{"a":${typed(inclusion, value, unboxedJson)}}""")
            }
        }

        @Test
        fun asProperty() = assertAnyHolderRoundTrip(As.PROPERTY)

        @Test
        fun asWrapperArray() = assertAnyHolderRoundTrip(As.WRAPPER_ARRAY)

        @Test
        fun asWrapperObject() = assertAnyHolderRoundTrip(As.WRAPPER_OBJECT)

        @Test
        fun listContents() {
            val mapper = mapper(As.PROPERTY)
            val values = cases.map { it.first }
            val expectedContents = cases.joinToString(",") { (value, unboxedJson) -> typed(As.PROPERTY, value, unboxedJson) }

            assertRoundTrip(
                mapper,
                AnyListHolder(ArrayList(values)),
                """{"l":["java.util.ArrayList",[$expectedContents]]}""",
            )
        }

        @Test
        fun mapValues() {
            val mapper = mapper(As.PROPERTY)
            val values = cases.mapIndexed { i, (value, _) -> "k$i" to value }.toMap(LinkedHashMap())
            val expectedContents = cases
                .mapIndexed { i, (value, unboxedJson) -> """"k$i":${typed(As.PROPERTY, value, unboxedJson)}""" }
                .joinToString(",")

            assertRoundTrip(
                mapper,
                AnyMapHolder(values),
                """{"m":{"@class":"java.util.LinkedHashMap",$expectedContents}}""",
            )
        }

        // Since the target types are final, properties declared with them are not affected by default typing.
        @Test
        fun notAppliedToDeclaredTypes() {
            val mapper = jacksonMapperBuilder()
                .activateDefaultTyping(typeValidator, DefaultTyping.NON_FINAL, As.PROPERTY)
                .build()

            assertRoundTrip(mapper, TypedHolder(1u, IntVc(2), JsonValueVc(3)), """{"u":1,"vc":2,"jv":103}""")
        }
    }

    @Nested
    inner class JsonTypeInfoTest {
        // The default validator denies `Any` as base type, so one that allows it is needed for the `Any` properties.
        private val mapper: ObjectMapper = jacksonMapperBuilder().polymorphicTypeValidator(typeValidator).build()

        @Test
        fun onAnyProperty() {
            cases.forEach { (value, unboxedJson) ->
                assertRoundTrip(
                    mapper,
                    AnnotatedAnyHolder(value),
                    """{"a":${typed(As.PROPERTY, value, unboxedJson)}}""",
                )
            }
        }

        @Test
        fun onAnyPropertyAsWrapperObject() {
            cases.forEach { (value, unboxedJson) ->
                assertRoundTrip(
                    mapper,
                    AnnotatedAnyWrapperObjectHolder(value),
                    """{"a":${typed(As.WRAPPER_OBJECT, value, unboxedJson)}}""",
                )
            }
        }

        // @JsonTypeInfo on a collection property applies to its contents
        @Test
        fun onUnsignedListContents() {
            assertRoundTrip(
                defaultMapper,
                AnnotatedUIntListHolder(listOf(1u, UInt.MAX_VALUE)),
                """{"l":[["kotlin.UInt",1],["kotlin.UInt",4294967295]]}""",
            )
        }

        @Test
        fun onValueClassListContents() {
            val id = IntVc::class.java.name

            assertRoundTrip(
                defaultMapper,
                AnnotatedValueClassListHolder(listOf(IntVc(1), IntVc(2))),
                """{"l":[["$id",1],["$id",2]]}""",
            )
        }

        @Test
        fun onJsonValueValueClassListContents() {
            val id = JsonValueVc::class.java.name

            assertRoundTrip(
                defaultMapper,
                AnnotatedJsonValueValueClassListHolder(listOf(JsonValueVc(1), JsonValueVc(2))),
                """{"l":[["$id",101],["$id",102]]}""",
            )
        }
    }

    data class TypedHolder(val u: UInt, val vc: IntVc, val jv: JsonValueVc)

    data class AnnotatedAnyHolder(@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS) val a: Any)

    data class AnnotatedAnyWrapperObjectHolder(
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = As.WRAPPER_OBJECT) val a: Any,
    )

    data class AnnotatedUIntListHolder(@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS) val l: List<UInt>)

    data class AnnotatedValueClassListHolder(@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS) val l: List<IntVc>)

    data class AnnotatedJsonValueValueClassListHolder(@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS) val l: List<JsonValueVc>)
}
