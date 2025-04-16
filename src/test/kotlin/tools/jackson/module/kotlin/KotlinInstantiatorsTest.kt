package tools.jackson.module.kotlin

import tools.jackson.databind.deser.std.StdValueInstantiator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class KotlinInstantiatorsTest {
    private val mapper = jacksonObjectMapper()
    private val deserConfig = mapper.deserializationConfig()

    private val kotlinInstantiators = KotlinInstantiators(
        ReflectionCache(10),
        nullToEmptyCollection = false,
        nullToEmptyMap = false,
        nullIsSameAsDefault = false,
        strictNullChecks = false
    )

    @Test
    fun `Provides default instantiator for Java class`() {
        val javaType = mapper.constructType(String::class.java)
        val defaultInstantiator = StdValueInstantiator(deserConfig, javaType)
        val instantiator = kotlinInstantiators.modifyValueInstantiator(
            deserConfig,
            deserConfig.classIntrospectorInstance().introspectForDeserialization(javaType).supplier(),
            defaultInstantiator
        )

        assertEquals(defaultInstantiator, instantiator)
    }

    @Test
    fun `Provides KotlinValueInstantiator for Kotlin class`() {
        class TestClass

        val javaType = mapper.constructType(TestClass::class.java)
        val instantiator = kotlinInstantiators.modifyValueInstantiator(
            deserConfig,
            deserConfig.classIntrospectorInstance().introspectForDeserialization(javaType).supplier(),
            StdValueInstantiator(deserConfig, javaType)
        )

        assertTrue(instantiator is StdValueInstantiator)
        assertTrue(instantiator::class == KotlinValueInstantiator::class)
    }

    @Test
    fun `Throws for Kotlin class when default instantiator isn't StdValueInstantiator`() {
        class TestClass
        class DefaultClass

        val subClassInstantiator = object : StdValueInstantiator(
            deserConfig,
            mapper.constructType(DefaultClass::class.java)
        ) {}

        assertThrows(IllegalStateException::class.java) {
            kotlinInstantiators.modifyValueInstantiator(
                deserConfig,
                deserConfig.classIntrospectorInstance()
                    .introspectForDeserialization(mapper.constructType(TestClass::class.java)).supplier(),
                subClassInstantiator
            )
        }
    }
}
