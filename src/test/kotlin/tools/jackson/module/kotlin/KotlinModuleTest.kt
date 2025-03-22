package tools.jackson.module.kotlin

import tools.jackson.module.kotlin.KotlinFeature.*
import tools.jackson.databind.json.JsonMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertNotNull

class KotlinModuleTest {
    // After the final migration is complete, this test will be removed.
    @Test
    fun strictNullChecksTests() {
        assertTrue(kotlinModule { enable(StrictNullChecks) }.strictNullChecks)
        assertTrue(kotlinModule { enable(NewStrictNullChecks) }.strictNullChecks)

        assertThrows<IllegalArgumentException> {
            kotlinModule {
                enable(StrictNullChecks)
                enable(NewStrictNullChecks)
            }
        }
    }

    @Test
    fun builder_Defaults() {
        val module = KotlinModule.Builder().build()

        assertEquals(512, module.reflectionCacheSize)
        assertFalse(module.nullToEmptyCollection)
        assertFalse(module.nullToEmptyMap)
        assertFalse(module.nullIsSameAsDefault)
        assertFalse(module.singletonSupport)
        assertFalse(module.strictNullChecks)
        assertFalse(module.kotlinPropertyNameAsImplicitName)
        assertFalse(module.useJavaDurationConversion)
    }

    @Test
    fun builder_SetAll() {
        val module = KotlinModule.Builder().apply {
            withReflectionCacheSize(123)
            enable(NullToEmptyCollection)
            enable(NullToEmptyMap)
            enable(NullIsSameAsDefault)
            enable(SingletonSupport)
            enable(NewStrictNullChecks)
            enable(KotlinPropertyNameAsImplicitName)
            enable(UseJavaDurationConversion)
        }.build()

        assertEquals(123, module.reflectionCacheSize)
        assertTrue(module.nullToEmptyCollection)
        assertTrue(module.nullToEmptyMap)
        assertTrue(module.nullIsSameAsDefault)
        assertTrue(module.singletonSupport)
        assertTrue(module.strictNullChecks)
        assertTrue(module.kotlinPropertyNameAsImplicitName)
        assertTrue(module.useJavaDurationConversion)
    }

    @Test
    fun builder_NullToEmptyCollection() {
        val module = KotlinModule.Builder().apply {
            enable(NullToEmptyCollection)
        }.build()

        assertTrue(module.nullToEmptyCollection)
    }

    @Test
    fun builder_NullToEmptyMap() {
        val module = KotlinModule.Builder().apply {
            enable(NullToEmptyMap)
        }.build()

        assertTrue(module.nullToEmptyMap)
    }

    @Test
    fun builder_NullIsSameAsDefault() {
        val module = KotlinModule.Builder().apply {
            enable(NullIsSameAsDefault)
        }.build()

        assertTrue(module.nullIsSameAsDefault)
    }

    @Test
    fun builder_EnableCanonicalSingletonSupport() {
        val module = KotlinModule.Builder().apply {
            enable(SingletonSupport)
        }.build()

        assertTrue(module.singletonSupport)
    }

    @Test
    fun builder_EnableStrictNullChecks() {
        val module = KotlinModule.Builder().apply {
            enable(NewStrictNullChecks)
        }.build()

        assertTrue(module.strictNullChecks)
    }

    @Test
    fun jdkSerializabilityTest() {
        val module = KotlinModule.Builder().apply {
            withReflectionCacheSize(123)
            enable(NullToEmptyCollection)
            enable(NullToEmptyMap)
            enable(NullIsSameAsDefault)
            enable(SingletonSupport)
            enable(NewStrictNullChecks)
        }.build()

        val serialized = jdkSerialize(module)
        val deserialized = jdkDeserialize<KotlinModule>(serialized)

        assertNotNull(deserialized)
        assertEquals(123, deserialized.reflectionCacheSize)
        assertTrue(deserialized.nullToEmptyCollection)
        assertTrue(deserialized.nullToEmptyMap)
        assertTrue(deserialized.nullIsSameAsDefault)
        assertTrue(deserialized.singletonSupport)
        assertTrue(deserialized.strictNullChecks)
    }

    @Test
    fun findAndRegisterModulesTest() {
        val mapper = JsonMapper.builder().findAndAddModules().build()
        assertTrue(mapper.registeredModules.any { it is KotlinModule })
    }
}
