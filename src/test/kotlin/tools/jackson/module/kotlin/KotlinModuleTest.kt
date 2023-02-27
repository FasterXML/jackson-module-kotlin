package tools.jackson.module.kotlin

import tools.jackson.module.kotlin.KotlinFeature.NullIsSameAsDefault
import tools.jackson.module.kotlin.KotlinFeature.NullToEmptyCollection
import tools.jackson.module.kotlin.KotlinFeature.NullToEmptyMap
import tools.jackson.module.kotlin.KotlinFeature.SingletonSupport
import tools.jackson.module.kotlin.KotlinFeature.StrictNullChecks
import tools.jackson.module.kotlin.SingletonSupport.CANONICALIZE
import tools.jackson.module.kotlin.SingletonSupport.DISABLED
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertNotNull

class KotlinModuleTest {
    /**
     * Ensure that the default Builder matches Feature default settings.
     */
    @Test
    fun builderDefaultsMatchFeatures() {
        val module = KotlinModule.Builder().build()

        assertEquals(module.reflectionCacheSize, 512)
        assertFalse(module.nullToEmptyCollection)
        assertFalse(module.nullToEmptyMap)
        assertFalse(module.nullIsSameAsDefault)
        assertEquals(module.singletonSupport, DISABLED)
        assertFalse(module.strictNullChecks)
    }

    @Test
    fun builder_Defaults() {
        val module = KotlinModule.Builder().build()

        assertEquals(512, module.reflectionCacheSize)
        assertFalse(module.nullToEmptyCollection)
        assertFalse(module.nullToEmptyMap)
        assertFalse(module.nullIsSameAsDefault)
        assertEquals(DISABLED, module.singletonSupport)
        assertFalse(module.strictNullChecks)
    }

    @Test
    fun builder_SetAll() {
        val module = KotlinModule.Builder().apply {
            withReflectionCacheSize(123)
            enable(NullToEmptyCollection)
            enable(NullToEmptyMap)
            enable(NullIsSameAsDefault)
            enable(SingletonSupport)
            enable(StrictNullChecks)
        }.build()

        assertEquals(123, module.reflectionCacheSize)
        assertTrue(module.nullToEmptyCollection)
        assertTrue(module.nullToEmptyMap)
        assertTrue(module.nullIsSameAsDefault)
        assertEquals(CANONICALIZE, module.singletonSupport)
        assertTrue(module.strictNullChecks)
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

        assertEquals(CANONICALIZE, module.singletonSupport)
    }

    @Test
    fun builder_EnableStrictNullChecks() {
        val module = KotlinModule.Builder().apply {
            enable(StrictNullChecks)
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
            enable(StrictNullChecks)
        }.build()

        val serialized = jdkSerialize(module)
        val deserialized = jdkDeserialize<KotlinModule>(serialized)

        assertNotNull(deserialized)
        assertEquals(123, deserialized.reflectionCacheSize)
        assertTrue(deserialized.nullToEmptyCollection)
        assertTrue(deserialized.nullToEmptyMap)
        assertTrue(deserialized.nullIsSameAsDefault)
        assertEquals(CANONICALIZE, deserialized.singletonSupport)
        assertTrue(deserialized.strictNullChecks)
    }
}
