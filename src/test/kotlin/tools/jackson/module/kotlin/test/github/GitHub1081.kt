package tools.jackson.module.kotlin.test.github

import org.junit.jupiter.api.Test
import tools.jackson.module.kotlin.defaultMapper
import tools.jackson.module.kotlin.readValue
import kotlin.test.assertEquals

class GitHub1081 {
    interface A {
        fun getValues(): Collection<String>
    }

    data class B(val values: List<String>) : A {
        override fun getValues(): Collection<String> = values.toSet()
    }

    @Test
    fun serializeDeserializeUsesPropertyGetterDeterministically() {
        repeat(100) {
            val original = B(listOf("a", "a", "a"))
            val json = defaultMapper.writeValueAsString(original)
            val parsed = defaultMapper.readValue<B>(json)
            assertEquals(original.values, parsed.values, "iteration $it produced $json")
        }
    }

    @Test
    fun serializeUsesListPropertyNotCollectionOverride() {
        val value = B(listOf("a", "a", "a"))
        assertEquals("""{"values":["a","a","a"]}""", defaultMapper.writeValueAsString(value))
    }
}
