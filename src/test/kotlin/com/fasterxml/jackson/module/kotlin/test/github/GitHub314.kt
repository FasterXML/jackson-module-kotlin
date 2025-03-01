package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import kotlin.test.Test
import kotlin.test.assertEquals

class GitHub314 {
    // Since Nothing? is compiled as a Void, it can be serialized by specifying ALLOW_VOID_VALUED_PROPERTIES
    data object NothingData {
        val data: Nothing? = null
    }

    @Test
    fun test() {
        val expected = """{"data":null}"""

        val withoutKotlinModule = jsonMapper { enable(MapperFeature.ALLOW_VOID_VALUED_PROPERTIES) }
        assertEquals(expected, withoutKotlinModule.writeValueAsString(NothingData))

        val withKotlinModule = jsonMapper {
            enable(MapperFeature.ALLOW_VOID_VALUED_PROPERTIES)
            addModule(kotlinModule())
        }

        assertEquals(expected, withKotlinModule.writeValueAsString(NothingData))
    }
}
