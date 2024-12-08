package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.core.exc.InputCoercionException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import java.math.BigInteger
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows

internal class UnsignedNumbersTests {

    val mapper: ObjectMapper = jacksonObjectMapper()

    @Test
    fun `test UByte`() {
        val json = mapper.writeValueAsString(UByte.MAX_VALUE)
        val deserialized = mapper.readValue<UByte>(json)
        assertThat(deserialized, equalTo(UByte.MAX_VALUE))
    }

    @Test
    fun `test UByte overflow`() {
        val json = mapper.writeValueAsString(UByte.MAX_VALUE + 1u)
        assertThrows(InputCoercionException::class.java) { mapper.readValue<UByte>(json) }
    }

    @Test
    fun `test UByte underflow`() {
        val json = mapper.writeValueAsString(-1)
        assertThrows(InputCoercionException::class.java) { mapper.readValue<UByte>(json) }
    }

    @Test
    fun `test UShort`() {
        val json = mapper.writeValueAsString(UShort.MAX_VALUE)
        val deserialized = mapper.readValue<UShort>(json)
        assertThat(deserialized, equalTo(UShort.MAX_VALUE))
    }

    @Test
    fun `test UShort overflow`() {
        val json = mapper.writeValueAsString(UShort.MAX_VALUE + 1u)
        assertThrows(InputCoercionException::class.java) {  mapper.readValue<UShort>(json) }
    }

    @Test
    fun `test UShort underflow`() {
        val json = mapper.writeValueAsString(-1)
        assertThrows(InputCoercionException::class.java) { mapper.readValue<UShort>(json) }
    }

    @Test
    fun `test UInt`() {
        val json = mapper.writeValueAsString(UInt.MAX_VALUE)
        val deserialized = mapper.readValue<UInt>(json)
        assertThat(deserialized, equalTo(UInt.MAX_VALUE))
    }

    @Test
    fun `test UInt overflow`() {
        val json = mapper.writeValueAsString(UInt.MAX_VALUE.toULong() + 1u)
        assertThrows(InputCoercionException::class.java) { mapper.readValue<UInt>(json) }
    }

    @Test
    fun `test UInt underflow`() {
        val json = mapper.writeValueAsString(-1)
        assertThrows(InputCoercionException::class.java) { mapper.readValue<UInt>(json) }
    }

    @Test
    fun `test ULong`() {
        val json = mapper.writeValueAsString(ULong.MAX_VALUE)
        val deserialized = mapper.readValue<ULong>(json)
        assertThat(deserialized, equalTo(ULong.MAX_VALUE))
    }

    @Test
    fun `test ULong overflow`() {
        val value = BigInteger(ULong.MAX_VALUE.toString()) + BigInteger.ONE
        val json = mapper.writeValueAsString(value)
        assertThrows(InputCoercionException::class.java) { mapper.readValue<ULong>(json) }
    }

    @Test
    fun `test ULong underflow`() {
        val json = mapper.writeValueAsString(-1)
        assertThrows(InputCoercionException::class.java) { mapper.readValue<ULong>(json) }
    }
}
