package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
import com.fasterxml.jackson.databind.SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature.UseJavaDurationConversion
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import java.time.Duration as JavaDuration
import kotlin.time.Duration as KotlinDuration

class DurationTests {
    private val objectMapper = jacksonObjectMapper { enable(UseJavaDurationConversion) }

    @Test
    fun `should serialize Kotlin duration using Java time module`() {
        val mapper = objectMapper.registerModule(JavaTimeModule()).disable(WRITE_DURATIONS_AS_TIMESTAMPS)

        val result = mapper.writeValueAsString(1.hours)

        assertEquals("\"PT1H\"", result)
    }

    @Test
    fun `should deserialize Kotlin duration`() {
        val mapper = objectMapper.registerModule(JavaTimeModule())

        val result = mapper.readValue<KotlinDuration>("\"PT1H\"")

        assertEquals(1.hours, result)
    }

    @Test
    fun `should serialize Kotlin duration inside list using Java time module`() {
        val mapper = objectMapper
            .registerModule(JavaTimeModule())
            .disable(WRITE_DURATIONS_AS_TIMESTAMPS)

        val result = mapper.writeValueAsString(listOf(1.hours, 2.hours, 3.hours))

        assertEquals("""["PT1H","PT2H","PT3H"]""", result)
    }

    @Test
    fun `should deserialize Kotlin duration inside list`() {
        val mapper = objectMapper.registerModule(JavaTimeModule())

        val result = mapper.readValue<List<KotlinDuration>>("""["PT1H","PT2H","PT3H"]""")

        assertContentEquals(listOf(1.hours, 2.hours, 3.hours), result)
    }

    @Test
    fun `should serialize Kotlin duration inside map using Java time module`() {
        val mapper = objectMapper
            .registerModule(JavaTimeModule())
            .disable(WRITE_DURATIONS_AS_TIMESTAMPS)

        val result = mapper.writeValueAsString(mapOf(
            "a" to 1.hours,
            "b" to 2.hours,
            "c" to 3.hours
        ))

        assertEquals("""{"a":"PT1H","b":"PT2H","c":"PT3H"}""", result)
    }

    @Test
    fun `should deserialize Kotlin duration inside map`() {
        val mapper = objectMapper.registerModule(JavaTimeModule())

        val result = mapper.readValue<Map<String, KotlinDuration>>("""{"a":"PT1H","b":"PT2H","c":"PT3H"}""")

        assertEquals(result["a"], 1.hours)
        assertEquals(result["b"], 2.hours)
        assertEquals(result["c"], 3.hours)
    }

    data class Meeting(
        val start: Instant,
        val duration: KotlinDuration,
    ) {
        companion object {
            @Suppress("unused")
            @JvmStatic
            @JsonCreator
            fun create(start: Instant, duration: KotlinDuration) = Meeting(start, duration)
        }
    }

    abstract class MeetingMixin(
        @Suppress("unused") @field:JsonFormat(shape = STRING)
        val duration: KotlinDuration,
    )

    @Test
    fun `should serialize Kotlin duration inside data class using Java time module`() {
        val mapper = objectMapper
            .registerModule(JavaTimeModule())
            .disable(WRITE_DATES_AS_TIMESTAMPS)
            .disable(WRITE_DURATIONS_AS_TIMESTAMPS)

        val result = mapper.writeValueAsString(Meeting(Instant.parse("2023-06-20T14:00:00Z"), 1.5.hours))

        assertEquals("""{"start":"2023-06-20T14:00:00Z","duration":"PT1H30M"}""", result)
    }

    @Test
    fun `should deserialize Kotlin duration inside data class`() {
        val mapper = objectMapper.registerModule(JavaTimeModule())

        val result = mapper.readValue<Meeting>("""{"start":"2023-06-20T14:00:00Z","duration":"PT1H30M"}""")

        assertEquals(result.start, Instant.parse("2023-06-20T14:00:00Z"))
        assertEquals(result.duration, 1.5.hours)
    }

    @Test
    fun `should deserialize Kotlin duration inside data class using mixin`() {
        val mapper = objectMapper
            .registerModule(JavaTimeModule())
            .addMixIn(Meeting::class.java, MeetingMixin::class.java)

        val meeting = mapper.readValue<Meeting>("""{"start":"2023-06-20T14:00:00Z","duration":"PT1H30M"}""")

        assertEquals(Instant.parse("2023-06-20T14:00:00Z"), meeting.start)
        assertEquals(1.5.hours, meeting.duration)
    }

    @Test
    fun `should serialize Kotlin duration inside data class using Java time module and mixin`() {
        val mapper = objectMapper
            .registerModule(JavaTimeModule())
            .disable(WRITE_DATES_AS_TIMESTAMPS)
            .addMixIn(Meeting::class.java, MeetingMixin::class.java)

        val result = mapper.writeValueAsString(Meeting(Instant.parse("2023-06-20T14:00:00Z"), 1.5.hours))

        assertEquals("""{"start":"2023-06-20T14:00:00Z","duration":"PT1H30M"}""", result)
    }

    data class JDTO(
        val plain: JavaDuration = JavaDuration.ofHours(1),
        val optPlain: JavaDuration? = JavaDuration.ofHours(1),
        @field:JsonFormat(shape = STRING)
        val shapeAnnotation: JavaDuration = JavaDuration.ofHours(1),
        @field:JsonFormat(shape = STRING)
        val optShapeAnnotation: JavaDuration? = JavaDuration.ofHours(1),
    )

    data class KDTO(
        val plain: KotlinDuration = 1.hours,
        val optPlain: KotlinDuration? = 1.hours,
        @field:JsonFormat(shape = STRING)
        val shapeAnnotation: KotlinDuration = 1.hours,
        @field:JsonFormat(shape = STRING)
        val optShapeAnnotation: KotlinDuration? = 1.hours,
    )

    @Test
    fun `should serialize Kotlin duration exactly as Java duration`() {
        val mapper = objectMapper.registerModule(JavaTimeModule())

        val jdto = JDTO()
        val kdto = KDTO()

        assertEquals(mapper.writeValueAsString(jdto), mapper.writeValueAsString(kdto))
    }

    data class DurationWithFormattedUnits(
        @field:JsonFormat(pattern = "HOURS") val formatted: KotlinDuration,
        val default: KotlinDuration,
    ) {
        companion object {
            @Suppress("unused")
            @JvmStatic
            @JsonCreator
            fun create(
                formatted: KotlinDuration,
                default: KotlinDuration,
            ) = DurationWithFormattedUnits(formatted, default)
        }
    }

    @Test
    fun `should deserialize using custom units specified by format annotation`() {
        val mapper = objectMapper.registerModule(JavaTimeModule())

        val actual = mapper.readValue<DurationWithFormattedUnits>("""{"formatted":1,"default":1}""")

        assertEquals(1.hours, actual.formatted)
        assertEquals(1.seconds, actual.default)
    }

    private fun jacksonObjectMapper(
        configuration: KotlinModule.Builder.() -> Unit,
    ) = ObjectMapper().registerModule(kotlinModule(configuration))
}
