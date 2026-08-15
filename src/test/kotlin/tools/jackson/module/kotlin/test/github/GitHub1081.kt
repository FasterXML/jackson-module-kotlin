package tools.jackson.module.kotlin.test.github

import org.junit.jupiter.api.Test
import tools.jackson.module.kotlin.KotlinMethodCollectorFix
import tools.jackson.module.kotlin.defaultMapper
import tools.jackson.module.kotlin.readValue
import tools.jackson.databind.introspect.MemberKey
import java.lang.reflect.Method
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GitHub1081 {
    interface A {
        fun getValues(): Collection<String>
    }

    data class B(val values: List<String>) : A {
        override fun getValues(): Collection<String> = values.toSet()
    }

    @Test
    fun serializeUsesListPropertyNotCollectionOverride() {
        val value = B(listOf("a", "a", "a"))
        assertEquals("""{"values":["a","a","a"]}""", defaultMapper.writeValueAsString(value))
    }

    @Test
    fun roundTripPreservesListValues() {
        val original = B(listOf("a", "a", "a"))
        val parsed = defaultMapper.readValue<B>(defaultMapper.writeValueAsString(original))
        assertEquals(original.values, parsed.values)
    }

    @Test
    fun shouldReplaceForKotlinPrefersListOverCollectionInBothDirections() {
        val (listGetter, collectionGetter) = conflictingGetValuesMethods()

        assertTrue(
            KotlinMethodCollectorFix.shouldReplaceForKotlin(collectionGetter, listGetter),
            "Collection getter should be replaced by List getter",
        )
        assertFalse(
            KotlinMethodCollectorFix.shouldReplaceForKotlin(listGetter, collectionGetter),
            "List getter should not be replaced by Collection getter",
        )
    }

    @Test
    fun selectKotlinAwareMethodsPrefersListRegardlessOfEncounterOrder() {
        val (listGetter, collectionGetter) = conflictingGetValuesMethods()
        val key = MemberKey(listGetter)

        val listFirst = KotlinMethodCollectorFix.selectKotlinAwareMethods(listOf(listGetter, collectionGetter))
        val collectionFirst = KotlinMethodCollectorFix.selectKotlinAwareMethods(listOf(collectionGetter, listGetter))

        assertEquals(listGetter, listFirst[key])
        assertEquals(listGetter, collectionFirst[key])
    }

    private fun conflictingGetValuesMethods(): Pair<Method, Method> {
        val getters = B::class.java.methods.filter { method ->
            method.name == "getValues" && method.parameterCount == 0
        }
        require(getters.size == 2) { "expected two getValues() methods, found ${getters.size}" }

        val listGetter = getters.first { java.util.List::class.java.isAssignableFrom(it.returnType) }
        val collectionGetter = getters.first {
            java.util.Collection::class.java.isAssignableFrom(it.returnType) &&
                !java.util.List::class.java.isAssignableFrom(it.returnType)
        }
        return listGetter to collectionGetter
    }
}
