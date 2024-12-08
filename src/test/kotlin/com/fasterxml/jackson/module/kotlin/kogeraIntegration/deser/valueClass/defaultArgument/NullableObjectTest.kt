package com.fasterxml.jackson.module.kotlin.kogeraIntegration.deser.valueClass.defaultArgument

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.kogeraIntegration.deser.valueClass.NullableObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class NullableObjectTest {
    companion object {
        val mapper = jacksonObjectMapper()
    }

    data class ByConstructor(
        val nnNn: NullableObject = NullableObject("foo"),
        val nnN: NullableObject = NullableObject(null),
        val nNn: NullableObject? = NullableObject("bar"),
        val nN: NullableObject? = null
    )

    @Test
    fun byConstructorTestFailing() {
        // #761(KT-57357) fixed
        assertThrows(Error::class.java) {
            assertEquals(ByConstructor(), mapper.readValue<ByConstructor>("{}"))
        }
    }

    data class ByFactory(
        val nnNn: NullableObject = NullableObject("foo"),
        val nnN: NullableObject = NullableObject(null),
        val nNn: NullableObject? = NullableObject("bar"),
        val nN: NullableObject? = null
    ) {
        companion object {
            @JvmStatic
            @JsonCreator
            fun creator(
                nn: NullableObject = NullableObject("foo"),
                nnN: NullableObject = NullableObject(null),
                nNn: NullableObject? = NullableObject("bar"),
                nN: NullableObject? = null
            ) = ByFactory(nn, nnN, nNn, nN)
        }
    }

    @Test
    fun byFactoryTest() {
        // #761(KT-57357) fixed
        assertThrows(Error::class.java) {
            assertEquals(ByFactory.creator(), mapper.readValue<ByFactory>("{}"))
        }
    }
}
