package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import java.util.function.IntSupplier


class TestGithub167 {
    val samObject = IntSupplier { 42 }

    @Test
    fun withKotlinExtension() {
        jacksonObjectMapper().writeValueAsString(samObject)
    }

    @Test
    fun withoutKotlinExtension() {
        ObjectMapper().writeValueAsString(samObject)
    }
}