package tools.jackson.module.kotlin

import com.fasterxml.jackson.module.kotlin.KotlinFeature.NullIsSameAsDefault
import com.fasterxml.jackson.module.kotlin.KotlinFeature.NullToEmptyCollection
import com.fasterxml.jackson.module.kotlin.KotlinFeature.NullToEmptyMap
import com.fasterxml.jackson.module.kotlin.KotlinFeature.SingletonSupport
import com.fasterxml.jackson.module.kotlin.KotlinFeature.StrictNullChecks
import com.fasterxml.jackson.module.kotlin.SingletonSupport.CANONICALIZE
import com.fasterxml.jackson.module.kotlin.SingletonSupport.DISABLED
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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
}
