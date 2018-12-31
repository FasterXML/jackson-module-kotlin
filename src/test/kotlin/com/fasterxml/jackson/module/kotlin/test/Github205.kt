@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Support for inline classes.
 * https://kotlinlang.org/docs/reference/inline-classes.html
 *
 * @author Tjeu Kayim
 */
class TestGithub205 {
    val mapper = jacksonObjectMapper()

    @Test
    fun readInlineClass() {
        val watt = Watt(1234)
        val json = "1234"
        val result = mapper.readValue<Watt>(json)
        assertEquals(watt, result)
    }

    @Test
    fun writeInlineClass() {
        val watt = Watt(1234)
        val json = mapper.writeValueAsString(watt)
        assertEquals("1234", json)
    }

    @Test
    fun implementingInterface() {
        // More complex inline class that implements an interface and has multiple properties.
        val obj = InlineAndImplementing(12)
        val json = mapper.writeValueAsString(obj)
        assertEquals("12", json)
        val result = mapper.readValue<InlineAndImplementing>(json)
        assertEquals(obj, result)
    }

    @Test
    fun keepExample() {
        // https://github.com/Kotlin/KEEP/blob/master/proposals/inline-classes.md#inline-classes-abi-jvm
        val obj = IC(12)
        val json = mapper.writeValueAsString(obj)
        assertEquals("12", json)
        val result = mapper.readValue<IC>(json)
        assertEquals(obj, result)
    }

    @Test
    fun nullableIC() {
        val obj = ICNullable("hello world")
        val json = mapper.writeValueAsString(obj)
        assertEquals("\"hello world\"", json)
        val result = mapper.readValue<ICNullable>(json)
        assertEquals(obj, result)
    }

    @Test
    fun nullableICNull() {
        val obj = ICNullable(null)
        val json = mapper.writeValueAsString(obj)
        assertEquals("null", json)
        // A weird edge case, one might expect to get ICNullabe(null).
        val result: ICNullable? = mapper.readValue(json)
        assertNull(result)
    }

    // TODO: Make it possible to override default behaviour with @JsonValue and @JsonCreator

    @Ignore
    @Test
    fun overrideJsonValue() {
        val obj = OverrideValue("hello")
        val json = mapper.writeValueAsString(obj)
        assertEquals("\"override\"", json)
        val result = mapper.readValue<OverrideValue>(json)
        assertEquals(OverrideValue("override"), result)
    }

    @Ignore
    @Test
    fun overrideJsonValueProp() {
        val obj = JsonValueAnnotated("hello")
        val json = mapper.writeValueAsString(obj)
        assertEquals("\"hello\"", json)
        val result = mapper.readValue<JsonValueAnnotated>(json)
        assertEquals(obj, result)
    }

    @Ignore
    @Test
    fun overrideJsonCreator() {
        val obj = OverrideCreator(12.34F)
        val json = mapper.writeValueAsString(obj)
        assertEquals("12.34", json)
        val result = mapper.readValue<OverrideCreator>(json)
        assertEquals(OverrideCreator.parse(0F), result)
    }
}

private inline class Watt(val value: Long)

private inline class OverrideValue(val bar: String) {
    @JsonValue
    fun toJson() = "override"
}

private inline class JsonValueAnnotated(@JsonValue val bar: String)

@Suppress("unused")
private inline class OverrideCreator(val float: Float) {
    companion object {
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        @JvmStatic
        fun parse(value: Float) = OverrideCreator(99.99F)
    }
}

private interface Printable {
    val foo: Int
    fun bar(x: String)
}

@Suppress("unused")
private inline class InlineAndImplementing(val x: Byte) : Printable {
    override val foo: Int
        get() = TODO()

    override fun bar(x: String): Unit = TODO()
}

private interface Base {
    fun base(s: String): Int
}

private inline class IC(val u: Int) : Base {
    fun simple(y: String) {}
    fun icInParameter(ic: IC, y: String) {}

    val simpleProperty get() = 42
    val propertyIC get() = IC(42)
    var mutablePropertyIC: IC
        get() = IC(42)
        set(value) {}

    override fun base(s: String): Int = 0
    override fun toString(): String = "IC = $u"
}

private inline class ICNullable(val s: String?)
