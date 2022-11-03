package tools.jackson.module.kotlin

import tools.jackson.core.json.JsonReadFeature
import tools.jackson.core.json.JsonWriteFeature
import tools.jackson.module.kotlin.KotlinFeature.NullIsSameAsDefault
import tools.jackson.module.kotlin.KotlinFeature.NullToEmptyCollection
import tools.jackson.module.kotlin.KotlinFeature.NullToEmptyMap
import tools.jackson.module.kotlin.KotlinFeature.SingletonSupport
import tools.jackson.module.kotlin.KotlinFeature.StrictNullChecks
import tools.jackson.module.kotlin.SingletonSupport.CANONICALIZE
import org.junit.Assert.assertNotNull
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DslTest {

    @Test
    fun createModuleWithoutUsingInitializer() {
        val module = _root_ide_package_.tools.jackson.module.kotlin.kotlinModule()
        assertNotNull(module)
    }

    @Test
    fun createModuleWithEmptyInitializer() {
        val module = _root_ide_package_.tools.jackson.module.kotlin.kotlinModule {}
        assertNotNull(module)
    }

    @Test
    fun createModuleWithBuilderOptions() {
        val module = _root_ide_package_.tools.jackson.module.kotlin.kotlinModule {
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
        val mapper = _root_ide_package_.tools.jackson.module.kotlin.jsonMapper()
        assertNotNull(mapper)
    }

    @Test
    fun createJsonMapperWithEmptyInitializer() {
        val mapper = _root_ide_package_.tools.jackson.module.kotlin.jsonMapper {}
        assertNotNull(mapper)
    }

    @Test
    fun createJsonMapperWithBuilderOptions() {
        val mapper = _root_ide_package_.tools.jackson.module.kotlin.jsonMapper {
            enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
            disable(JsonWriteFeature.QUOTE_FIELD_NAMES)
            configure(JsonReadFeature.ALLOW_SINGLE_QUOTES, true)

            addModule(_root_ide_package_.tools.jackson.module.kotlin.kotlinModule {
                enable(NullIsSameAsDefault)
            })
        }

        assertNotNull(mapper)
        assertTrue(mapper.isEnabled(JsonReadFeature.ALLOW_JAVA_COMMENTS))
        assertFalse(mapper.isEnabled(JsonWriteFeature.QUOTE_FIELD_NAMES))
        assertTrue(mapper.isEnabled(JsonReadFeature.ALLOW_SINGLE_QUOTES))
        assertTrue(mapper.registeredModuleIds.any { it == "tools.jackson.module.kotlin.KotlinModule" })
    }
}
