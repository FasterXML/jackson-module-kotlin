package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.core.json.JsonWriteFeature
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
            reflectionCacheSize(123)
            nullToEmptyCollection(true)
            nullToEmptyMap(true)
            nullIsSameAsDefault(true)
            singletonSupport(CANONICALIZE)
            strictNullChecks(true)
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
    fun creatJsonMapperWithEmptyInitializer() {
        val mapper = jsonMapper {}
        assertNotNull(mapper)
    }

    @Test
    fun creatJsonMapperWithBuilderOptions() {
        val mapper = jsonMapper {
            enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
            disable(JsonWriteFeature.QUOTE_PROPERTY_NAMES)
            configure(JsonReadFeature.ALLOW_SINGLE_QUOTES, true)

            addModule(kotlinModule {
                nullIsSameAsDefault(true)
            })
        }

        assertNotNull(mapper)
        assertTrue(mapper.isEnabled(JsonReadFeature.ALLOW_JAVA_COMMENTS))
        assertFalse(mapper.isEnabled(JsonWriteFeature.QUOTE_PROPERTY_NAMES))
        assertTrue(mapper.isEnabled(JsonReadFeature.ALLOW_SINGLE_QUOTES))
        assertTrue(mapper.registeredModules.any { it.moduleName == "jackson-module-kotlin" })
    }
}
