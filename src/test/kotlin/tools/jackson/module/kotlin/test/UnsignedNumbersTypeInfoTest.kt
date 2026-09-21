package tools.jackson.module.kotlin.test

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.As
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tools.jackson.databind.DefaultTyping
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import tools.jackson.module.kotlin.jacksonMapperBuilder
import tools.jackson.module.kotlin.readValue

/**
 * Unsigned numbers stored in a position that requires polymorphic type information
 * (an `Any` property under default typing, or a property annotated with `@JsonTypeInfo`)
 * are written with a type id, as other scalar values are, e.g. `["kotlin.UInt",1]`.
 *
 * Reading such input already worked before, so the round-trip tests check that
 * the written form is the one the deserializers expect.
 */
internal class UnsignedNumbersTypeInfoTest {
    data class AnyHolder(val value: Any)

    data class AnnotatedHolder(@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS) val value: Any)

    data class AnnotatedWrapperObjectHolder(
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = As.WRAPPER_OBJECT) val value: Any,
    )

    private val typeValidator = BasicPolymorphicTypeValidator.builder().allowIfBaseType(Any::class.java).build()

    private val defaultTypingMapper = jacksonMapperBuilder()
        .activateDefaultTyping(typeValidator, DefaultTyping.OBJECT_AND_NON_CONCRETE, As.PROPERTY)
        .build()

    // The default validator denies `Any` as base type, so one that allows it is needed for the `Any` properties.
    private val annotatedMapper = jacksonMapperBuilder().polymorphicTypeValidator(typeValidator).build()

    abstract inner class ForType<T : Any>(private val value: T, private val json: String) {
        private val id = value::class.java.name

        @Test
        fun `default typing - type id is written`() {
            assertEquals("""{"value":["$id",$json]}""", defaultTypingMapper.writeValueAsString(AnyHolder(value)))
        }

        @Test
        fun `default typing - round trip`() {
            val expected = AnyHolder(value)
            val json = defaultTypingMapper.writeValueAsString(expected)

            assertEquals(expected, defaultTypingMapper.readValue<AnyHolder>(json))
        }

        @Test
        fun `JsonTypeInfo as property - type id is written`() {
            assertEquals("""{"value":["$id",$json]}""", annotatedMapper.writeValueAsString(AnnotatedHolder(value)))
        }

        @Test
        fun `JsonTypeInfo as property - round trip`() {
            val expected = AnnotatedHolder(value)
            val json = annotatedMapper.writeValueAsString(expected)

            assertEquals(expected, annotatedMapper.readValue<AnnotatedHolder>(json))
        }

        @Test
        fun `JsonTypeInfo as wrapper object - type id is written`() {
            assertEquals(
                """{"value":{"$id":$json}}""",
                annotatedMapper.writeValueAsString(AnnotatedWrapperObjectHolder(value)),
            )
        }

        @Test
        fun `JsonTypeInfo as wrapper object - round trip`() {
            val expected = AnnotatedWrapperObjectHolder(value)
            val json = annotatedMapper.writeValueAsString(expected)

            assertEquals(expected, annotatedMapper.readValue<AnnotatedWrapperObjectHolder>(json))
        }
    }

    @Nested
    inner class ForUByte : ForType<UByte>(UByte.MAX_VALUE, "255")

    @Nested
    inner class ForUShort : ForType<UShort>(UShort.MAX_VALUE, "65535")

    @Nested
    inner class ForUInt : ForType<UInt>(UInt.MAX_VALUE, "4294967295")

    @Nested
    inner class ForULong : ForType<ULong>(ULong.MAX_VALUE, "18446744073709551615")

    data class AnnotatedListHolder(@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS) val values: List<UInt>)

    // @JsonTypeInfo on a collection property applies to its contents,
    // so here the serializer is selected by the declared unsigned type rather than by the runtime type.
    @Test
    fun `JsonTypeInfo on collection contents`() {
        val expected = AnnotatedListHolder(listOf(1u, UInt.MAX_VALUE))
        val json = annotatedMapper.writeValueAsString(expected)

        assertEquals("""{"values":[["kotlin.UInt",1],["kotlin.UInt",4294967295]]}""", json)
        assertEquals(expected, annotatedMapper.readValue<AnnotatedListHolder>(json))
    }

    data class DeclaredHolder(val value: UInt, val nullable: UInt?)

    // The unsigned types are final, so default typing does not apply to properties declared with them.
    @Test
    fun `not applied to declared properties under NON_FINAL`() {
        val mapper = jacksonMapperBuilder()
            .activateDefaultTyping(typeValidator, DefaultTyping.NON_FINAL, As.PROPERTY)
            .build()
        val expected = DeclaredHolder(1u, 2u)
        val json = mapper.writeValueAsString(expected)

        assertEquals("""{"value":1,"nullable":2}""", json)
        assertEquals(expected, mapper.readValue<DeclaredHolder>(json))
    }
}
