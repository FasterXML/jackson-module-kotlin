package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.convertValue
import kotlin.test.Test
import kotlin.test.assertNull

class GitHub757 {
    @Test
    fun test() {
        val kotlinModule = KotlinModule.Builder()
            .enable(KotlinFeature.StrictNullChecks)
            .build()
        val mapper = JsonMapper.builder()
            .addModule(kotlinModule)
            .build()
        val convertValue = mapper.convertValue<String?>(null)
        assertNull(convertValue)
    }
}
