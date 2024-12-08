package com.fasterxml.jackson.module.kotlin.kogeraIntegration.deser.valueClass

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.module.kotlin.WrapsNullableValueClassDeserializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlin.reflect.jvm.internal.KotlinReflectionInternalError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.jupiter.api.Test

class NullableObjectEdgeCases {
    @JvmInline
    value class VC(val v: String?)

    class NullValueDeserializer : StdDeserializer<VC>(VC::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): VC =
            TODO("Not yet implemented")

        override fun getNullValue(ctxt: DeserializationContext): VC = nv

        companion object {
            val nv = VC("nv")
        }
    }

    data class NullValue(
        @field:JsonDeserialize(using = NullValueDeserializer::class)
        val nn: VC,
        @field:JsonDeserialize(using = NullValueDeserializer::class)
        val n: VC?
    )

    @Test
    fun nullValueIsUsedPreferentially() {
        val result = jacksonObjectMapper().readValue<NullValue>("""{"nn":null,"n":null}""")
        assertEquals(NullValue(NullValueDeserializer.nv, NullValueDeserializer.nv), result)
    }

    class NullsSkipDeserializerWrapsNullable : WrapsNullableValueClassDeserializer<VC>(VC::class) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): VC {
            TODO("Not yet implemented")
        }

        override fun getBoxedNullValue(): VC? = null
    }

    data class NullsSkip(
        @field:JsonSetter(nulls = Nulls.SKIP)
        @field:JsonDeserialize(using = NullsSkipDeserializerWrapsNullable::class)
        val nn: VC = VC("skip"),
        @field:JsonSetter(nulls = Nulls.SKIP)
        val n: VC? = VC("skip")
    )

    // There is a problem with #51, so it is a failing test.
    @Test
    fun `Nulls_SKIP works`() {
        assertThrows("#761(KT-57357) fixed", KotlinReflectionInternalError::class.java) {
            val result = jacksonObjectMapper().readValue<NullsSkip>("""{"nn":null,"n":null}""")
            assertEquals(NullValue(VC("skip"), VC("skip")), result)
        }
    }
}
