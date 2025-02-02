package com.fasterxml.jackson.module.kotlin.kogeraIntegration.deser.valueClass.mapKey.keyDeserializer.byAnnotation

import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.defaultMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import com.fasterxml.jackson.databind.KeyDeserializer as JacksonKeyDeserializer

class SpecifiedForClassTest {
    @JsonDeserialize(keyUsing = Value.KeyDeserializer::class)
    @JvmInline
    value class Value(val v: Int) {
        class KeyDeserializer : JacksonKeyDeserializer() {
            override fun deserializeKey(key: String, ctxt: DeserializationContext) = Value(key.toInt() + 100)
        }
    }

    @Test
    fun directDeserTest() {
        val result = defaultMapper.readValue<Map<Value, String?>>("""{"1":null}""")

        assertEquals(mapOf(Value(101) to null), result)
    }

    data class Wrapper(val v: Map<Value, String?>)

    @Test
    fun paramDeserTest() {
        val mapper = jacksonObjectMapper()
        val result = mapper.readValue<Wrapper>("""{"v":{"1":null}}""")

        assertEquals(Wrapper(mapOf(Value(101) to null)), result)
    }
}
