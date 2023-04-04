package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import kotlin.test.assertEquals

class GitHub618 {
    @JsonSerialize(using = V.Serializer::class)
    @JvmInline
    value class V(val value: String) {
        class Serializer : StdSerializer<V>(V::class.java) {
            override fun serialize(p0: V, p1: JsonGenerator, p2: SerializerProvider) {
                p1.writeString(p0.toString())
            }
        }
    }

    data class D(val v: V?)

    @Test
    fun test() {
        val mapper = jacksonObjectMapper()
        // expected: {"v":null}, but NullPointerException thrown
        assertEquals("""{"v":null}""", mapper.writeValueAsString(D(null)))
    }
}
