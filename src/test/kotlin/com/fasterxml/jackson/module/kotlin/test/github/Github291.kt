package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.Test
import kotlin.reflect.full.createInstance
import kotlin.test.assertNotNull

class Github291 {

    @Test
    fun kotlinModuleCouldBeCreatedByCreateInstance() {
        val kotlinModule = KotlinModule::class.createInstance()
        assertNotNull(kotlinModule)
    }
}