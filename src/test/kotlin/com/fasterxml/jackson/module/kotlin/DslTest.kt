package com.fasterxml.jackson.module.kotlin

import tools.jackson.core.json.JsonReadFeature
import tools.jackson.core.json.JsonWriteFeature
import com.fasterxml.jackson.module.kotlin.KotlinFeature.NullIsSameAsDefault
import com.fasterxml.jackson.module.kotlin.KotlinFeature.NullToEmptyCollection
import com.fasterxml.jackson.module.kotlin.KotlinFeature.NullToEmptyMap
import com.fasterxml.jackson.module.kotlin.KotlinFeature.SingletonSupport
import com.fasterxml.jackson.module.kotlin.KotlinFeature.StrictNullChecks
import com.fasterxml.jackson.module.kotlin.SingletonSupport.CANONICALIZE
import org.junit.Assert.assertNotNull
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DslTest {

    @Test
    fun createModuleWithoutUsingInitializer() {
        val module = kotlinModule()
        assertNotNull(module)
    }

    @Test
    fun createModuleWithEmptyInitializer() {
        val module = kotlinModule {}
        assertNotNull(module)
    }

    @Test
    fun createModuleWithBuilderOptions() {
        val module = kotlinModule {
            withReflectionCacheSize(123)
            enable(NullToEmptyCollection)
            enable(NullToEmptyMap)
            enable(NullIsSameAsDefault)
            enable(SingletonSupport)
            enable(StrictNullChecks)
        }

        assertNotNull(module)
        assertEquals(module.reflectionCacheSize, 123)
        assertTrue(module.nullToEmptyCollection)
        assertTrue(module.nullToEmptyMap)
        assertTrue(module.nullIsSameAsDefault)
        assertEquals(module.singletonSupport, CANONICALIZE)
        assertTrue(module.strictNullChecks)
    }

    @Test
    fun createJsonMapperWithoutUsingInitializer() {
        val mapper = jsonMapper()
        assertNotNull(mapper)
    }

    @Test
    fun createJsonMapperWithEmptyInitializer() {
        val mapper = jsonMapper {}
        assertNotNull(mapper)
    }

    @Test
    fun createJsonMapperWithBuilderOptions() {
        val mapper = jsonMapper {
            enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
            disable(JsonWriteFeature.QUOTE_PROPERTY_NAMES)
            configure(JsonReadFeature.ALLOW_SINGLE_QUOTES, true)

            addModule(kotlinModule {
                enable(NullIsSameAsDefault)
            })
        }

        assertNotNull(mapper)
        assertTrue(mapper.isEnabled(JsonReadFeature.ALLOW_JAVA_COMMENTS))
        assertFalse(mapper.isEnabled(JsonWriteFeature.QUOTE_PROPERTY_NAMES))
        assertTrue(mapper.isEnabled(JsonReadFeature.ALLOW_SINGLE_QUOTES))
        assertTrue(mapper.registeredModules.any { it.moduleName == "com.fasterxml.jackson.module.kotlin.KotlinModule" })
    }
}
