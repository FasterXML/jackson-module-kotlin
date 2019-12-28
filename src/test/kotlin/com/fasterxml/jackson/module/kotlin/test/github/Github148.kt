package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import kotlin.test.assertEquals

class TestGithub148 {
    enum class CorrectType {
        TYPEA,
        TYPEB,
    }

    enum class IncorrectType {
        TYPEA {
            override fun desc() = "type a"
        },

        TYPEB {
            override fun desc() = "type b"
        };

        abstract fun desc(): String
    }

    data class CorrentBean(
        val name: String,
        val type: CorrectType
    )

    data class IncorrentBean(
        val name: String,
        val type: IncorrectType
    )

    class DemoApplicationTests {
        val objectMapper = jacksonObjectMapper()

        @Test
        fun correntBean() {
            assertEquals("{\"name\":\"corrent\",\"type\":\"TYPEA\"}", objectMapper.writeValueAsString(CorrentBean("corrent", CorrectType.TYPEA)))
        }

        @Test
        fun incorrentBean() {
            assertEquals("{\"name\":\"incorrent\",\"type\":\"TYPEA\"}", objectMapper.writeValueAsString(IncorrentBean("incorrent", IncorrectType.TYPEA)))
        }
    }
}