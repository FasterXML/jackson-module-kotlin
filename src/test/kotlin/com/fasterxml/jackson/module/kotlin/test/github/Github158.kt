package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test

class TestGithub158 {
    enum class SampleImpl constructor(override val value: String): Sample {
        One("oney"),
        Two("twoey")
    }

    interface Sample {
        val value: String
    }

    data class SampleContainer(@JsonDeserialize(`as` = SampleImpl::class) val sample: Sample)

    @Test
    fun testEnumSerDeser() {
        val mapper = jacksonObjectMapper()

        val json = mapper.writeValueAsString(SampleContainer(SampleImpl.One))
        println(json)
        val obj = mapper.readValue<SampleContainer>(json)

    }
}