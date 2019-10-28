package com.fasterxml.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.*
import org.junit.Test

class TestCasesFromSlack1 {
    data class Host @JsonCreator constructor(
            @param:JsonProperty("id") @get:JsonProperty("id") val id: String,
            @param:JsonProperty("name") @get:JsonProperty("name") val name: String)

    data class Event @JsonCreator constructor(
            @param:JsonProperty("host") @get:JsonProperty("host") val host: Host,
            @param:JsonProperty("activity") @get:JsonProperty("activity") val activity: String,
            @param:JsonProperty("invited") @get:JsonProperty("invited") val invited: List<Guest>)

    data class Guest @JsonCreator constructor(
            @param:JsonProperty("id") @get:JsonProperty("id") val id: String,
            @param:JsonProperty("name") @get:JsonProperty("name") val name: String,
            @param:JsonProperty("rsvp") @get:JsonProperty("rsvp") var rsvp: RSVP)

    enum class RSVP(val nameKey: String) {
        going("rsvp.going"), maybe("rsvp.maybe"), interested("rsvp.interested")
    }

    @Test fun testCzarSpringThing1() {
        ObjectMapper().readValue<Event>("""
           {"host":{"id":"host123","name":"A Czar"},"activity":"Kotlin Programming","invited":[{"id":"Guest1","name":"Mr Kotlin","rsvp": "going"}]}
        """)

        jacksonObjectMapper().readValue<Event>("""
           {"host":{"id":"host123","name":"A Czar"},"activity":"Kotlin Programming","invited":[{"id":"Guest1","name":"Mr Kotlin","rsvp": "going"}]}
        """)
    }
}

class TestCasesFromSlack2 {
    data class Host  constructor(
           val id: String,
           val name: String)

    data class Event  constructor(
            val host: Host,
            val activity: String,
            val invited: List<Guest>)

    data class Guest constructor(
           val id: String,
           val name: String,
           var rsvp: RSVP = RSVP.going)

    enum class RSVP(val nameKey: String) {
        going("rsvp.going"), maybe("rsvp.maybe"), interested("rsvp.interested")
    }

    @Test fun testCzarSpringThing2() {
        jacksonObjectMapper().readValue<Event>("""
           {"host":{"id":"host123","name":"A Czar"},"activity":"Kotlin Programming","invited":[{"id":"Guest1","name":"Mr Kotlin","rsvp": "going"}]}
        """)
    }
}