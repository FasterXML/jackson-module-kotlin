package com.fasterxml.jackson.module.kotlin.kogeraIntegration.ser.valueClass.jsonInclude

@JvmInline
value class Primitive(val v: Int)

@JvmInline
value class NonNullObject(val v: String)

@JvmInline
value class NullableObject(val v: String?)
