package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KotlinInstantiatorsTest {
    private val mapper = jacksonObjectMapper()

    private val kotlinInstantiators = KotlinInstantiators(
        ReflectionCache(10),
        nullToEmptyCollection = false,
        nullToEmptyMap = false,
        nullIsSameAsDefault = false,
        strictNullChecks = false
    )

    private class DefaultClass

    private val deserConfig = mapper.deserializationConfig
    private val defaultInstantiator = object : StdValueInstantiator(
        deserConfig,
        mapper.constructType(DefaultClass::class.java)
    ) {}

    @Test
    fun `Provides default instantiator for Java class`() {
        val instantiator = kotlinInstantiators.findValueInstantiator(
            deserConfig,
            deserConfig.introspect(mapper.constructType(String::class.java)),
            defaultInstantiator
        )

        assertEquals(defaultInstantiator, instantiator)
    }

    @Test
    fun `Provides KotlinValueInstantiator for Kotlin class`() {
        class TestClass

        val instantiator = kotlinInstantiators.findValueInstantiator(
            deserConfig,
            deserConfig.introspect(mapper.constructType(TestClass::class.java)),
            defaultInstantiator
        )

        assertTrue(instantiator is StdValueInstantiator)
        assertTrue(instantiator::class == KotlinValueInstantiator::class)
    }
}