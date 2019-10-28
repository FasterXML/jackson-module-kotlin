package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.KeyDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.*
import org.junit.Test

private data class Github57Data(val map: Map<Pair<String, String>, String>)

class TestGithub57 {
    @Test
    fun testProblemsWithMaps() {
        val mapper = jacksonObjectMapper().registerModule(KotlinPairKeySerializerModule())
        val test = Github57Data(mapOf(Pair("p1", "p2") to "value1"))
        val jsonString = mapper.writeValueAsString(test) //works: {"map":{"(string1, string2)":"string3"}}
        mapper.readValue<Github57Data>(jsonString)
    }

    class KotlinPairKeyDeserializer: KeyDeserializer() {
        override fun deserializeKey(key: String, context: DeserializationContext): Any {
            return if (key.startsWith('(') && key.endsWith(')')) {
                val parts = key.substring(1, key.length-2).split(',')
                if (parts.size != 2) {
                    throw IllegalStateException("Pair() expects a serialized format of '(first,second)', cannot understand '$key'")
                }
                Pair(parts[0], parts[1])
            } else {
                throw IllegalStateException("Pair() expects a serialized format of '(first,second)', cannot understand '$key'")
            }
        }
    }

    class KotlinPairKeySerializerModule : SimpleModule() {
        init {
            addKeyDeserializer(Pair::class.java, KotlinPairKeyDeserializer())
        }
    }
}