package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.test.assertEquals

class TestGithub181 {
    enum class HealthStatus {
        FAILED,
        OK
    }

    data class HealthStatusMap @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor (val statuses: Map<String, HealthStatus>)
        : Map<String, HealthStatus> by statuses {

        fun isPassing() = statuses.all { (_, v) -> v == HealthStatus.OK }
    }

    @Test
    fun testReflectionExceptionOnDelegatedMap() {
        val testInstance = HealthStatusMap(mapOf("failed" to HealthStatus.FAILED, "okey dokey" to HealthStatus.OK))
        val json = jacksonObjectMapper().writeValueAsString(testInstance)
        assertEquals("{\"failed\":\"FAILED\",\"okey dokey\":\"OK\"}", json)
        val newInstance = jacksonObjectMapper().readValue<HealthStatusMap>(json)
        assertEquals(testInstance, newInstance)
    }
}