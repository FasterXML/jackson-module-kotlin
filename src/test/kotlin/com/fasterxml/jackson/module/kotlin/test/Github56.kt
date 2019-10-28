package com.fasterxml.jackson.module.kotlin.test

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import com.fasterxml.jackson.module.kotlin.*
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

private data class TestGalleryWidget_BAD(
        val widgetReferenceId: String,
        @JsonUnwrapped var gallery: TestGallery
)

private data class TestGalleryWidget_GOOD(val widgetReferenceId: String) {
    @JsonUnwrapped lateinit var gallery: TestGallery
}


@JsonInclude(JsonInclude.Include.NON_EMPTY)
private data class TestGallery(
        val id: String? = null,
        val headline: String? = null,
        val intro: String? = null,
        val role: String? = null,
        val images: List<TestImage>? = null
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
private data class TestImage(
        val id: String? = null,
        val escenicId: String? = null,
        val caption: String? = null,
        val copyright: String? = null,
        val crops: Map<String, String>? = null
)

class TestGithub56 {
    lateinit var mapper: ObjectMapper

    private val gallery = TestGallery(
            id = "id",
            headline = "headline",
            intro = "intro",
            role = "role",
            images = listOf(
                    TestImage(id = "testImage1"),
                    TestImage(id = "testImage2")
            )
    )
    val validJson = """
         {"widgetReferenceId":"widgetReferenceId","id":"id","headline":"headline","intro":"intro","role":"role","images":[{"id":"testImage1"},{"id":"testImage2"}]}
    """.trim()

    @Before
    fun setUp() {
        mapper = jacksonObjectMapper()
    }

    @Test
    fun serializes() {
        val result = mapper.writeValueAsString(TestGalleryWidget_BAD("widgetReferenceId", gallery))
        assertEquals(validJson, result)
    }

    @Test(expected = InvalidDefinitionException::class)
    fun deserializesWithError() {
        mapper.readValue<TestGalleryWidget_BAD>(validJson)
    }

    @Test
    fun deserializesCorrectly() {
        mapper.readValue<TestGalleryWidget_GOOD>(validJson)
    }
}