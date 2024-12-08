package tools.jackson.module.kotlin.test

import tools.jackson.core.exc.InputCoercionException
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigInteger
import kotlin.test.assertEquals

internal class UnsignedNumbersOnKeyTest {
    companion object {
        val MAPPER = jacksonObjectMapper()
    }

    class ForUByte {
        private fun makeSrc(v: Int): String = MAPPER.writeValueAsString(mapOf(v to 0))

        @Test
        fun test() {
            val actual = MAPPER.readValue<Map<UByte, UByte>>(makeSrc(UByte.MAX_VALUE.toInt()))
            assertEquals(mapOf(UByte.MAX_VALUE to 0.toUByte()), actual)
        }

        @Test
        fun overflow() {
            assertThrows(InputCoercionException::class.java) {
                MAPPER.readValue<Map<UByte, UByte>>(makeSrc(UByte.MAX_VALUE.toInt() + 1))
            }
        }

        @Test
        fun underflow() {
            assertThrows(InputCoercionException::class.java) {
                MAPPER.readValue<Map<UByte, UByte>>(makeSrc(-1))
            }
        }
    }

    class ForUShort {
        private fun makeSrc(v: Int): String = MAPPER.writeValueAsString(mapOf(v to 0))

        @Test
        fun test() {
            val actual = MAPPER.readValue<Map<UShort, UShort>>(makeSrc(UShort.MAX_VALUE.toInt()))
            assertEquals(mapOf(UShort.MAX_VALUE to 0.toUShort()), actual)
        }

        @Test
        fun overflow() {
            assertThrows(InputCoercionException::class.java) {
                MAPPER.readValue<Map<UShort, UShort>>(makeSrc(UShort.MAX_VALUE.toInt() + 1))
            }
        }

        @Test
        fun underflow() {
            assertThrows(InputCoercionException::class.java) {
                MAPPER.readValue<Map<UShort, UShort>>(makeSrc(-1))
            }
        }
    }

    class ForUInt {
        private fun makeSrc(v: Long): String = MAPPER.writeValueAsString(mapOf(v to 0))

        @Test
        fun test() {
            val actual = MAPPER.readValue<Map<UInt, UInt>>(makeSrc(UInt.MAX_VALUE.toLong()))
            assertEquals(mapOf(UInt.MAX_VALUE to 0.toUInt()), actual)
        }

        @Test
        fun overflow() {
            assertThrows(InputCoercionException::class.java) {
                MAPPER.readValue<Map<UInt, UInt>>(makeSrc(UInt.MAX_VALUE.toLong() + 1L))
            }
        }

        @Test
        fun underflow() {
            assertThrows(InputCoercionException::class.java) {
                MAPPER.readValue<Map<UInt, UInt>>(makeSrc(-1L))
            }
        }
    }

    class ForULong {
        private fun makeSrc(v: BigInteger): String = MAPPER.writeValueAsString(mapOf(v to 0))

        @Test
        fun test() {
            val actual = MAPPER.readValue<Map<ULong, ULong>>(makeSrc(BigInteger(ULong.MAX_VALUE.toString())))
            assertEquals(mapOf(ULong.MAX_VALUE to 0.toULong()), actual)
        }

        @Test
        fun overflow() {
            assertThrows(InputCoercionException::class.java) {
                MAPPER.readValue<Map<ULong, ULong>>(makeSrc(BigInteger(ULong.MAX_VALUE.toString()) + BigInteger.ONE))
            }
        }

        @Test
        fun underflow() {
            assertThrows(InputCoercionException::class.java) {
                MAPPER.readValue<Map<ULong, ULong>>(makeSrc(BigInteger.valueOf(-1L)))
            }
        }
    }
}
