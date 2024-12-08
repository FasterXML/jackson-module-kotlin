package tools.jackson.module.kotlin.test.github

import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.KeyDeserializer
import tools.jackson.databind.module.SimpleModule
import tools.jackson.module.kotlin.*
import org.junit.jupiter.api.Test

class TestGithub57 {
    data class Github57Data(val map: Map<Pair<String, String>, String>)

    @Test
    fun testProblemsWithMaps() {
        val mapper = jacksonMapperBuilder().addModule(KotlinPairKeySerializerModule())
                .build()
        val test = Github57Data(mapOf(Pair("p1", "p2") to "value1"))
        val jsonString = mapper.writeValueAsString(test) //works: {"map":{"(string1, string2)":"string3"}}
        val result = mapper.readValue<Github57Data>(jsonString)
        assert(result == test)
    }

    class KotlinPairKeyDeserializer: KeyDeserializer() {
        override fun deserializeKey(key: String, context: DeserializationContext): Any {
            return if (key.startsWith('(') && key.endsWith(')')) {
                val parts = key.substring(1, key.length-1).split(", ")
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
