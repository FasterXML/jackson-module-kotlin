package com.fasterxml.jackson.module.kotlin

import java.math.BigInteger

fun Short.asUByte() = when {
    this >= 0 && this <= UByte.MAX_VALUE.toShort() -> this.toUByte()
    else -> null
}

fun Int.asUShort() = when {
    this >= 0 && this <= UShort.MAX_VALUE.toInt() -> this.toUShort()
    else -> null
}

fun Long.asUInt() = when {
    this >= 0 && this <= UInt.MAX_VALUE.toLong() -> this.toUInt()
    else -> null
}

private val uLongMaxValue = BigInteger(ULong.MAX_VALUE.toString())
fun BigInteger.asULong() = when {
    this >= BigInteger.ZERO && this <= uLongMaxValue -> this.toLong().toULong()
    else -> null
}
