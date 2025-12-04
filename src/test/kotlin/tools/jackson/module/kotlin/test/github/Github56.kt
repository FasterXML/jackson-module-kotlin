package tools.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonUnwrapped
import org.junit.jupiter.api.Test
import tools.jackson.module.kotlin.defaultMapper
import tools.jackson.module.kotlin.readValue
import kotlin.test.assertEquals

class TestGithub56 {
    data class TestGalleryWidget_BAD(
            val widgetReferenceId: String,
            // IMPORTANT! Need _at least_ @get one (@param optional, not sufficient)
            // (see https://github.com/FasterXML/jackson-databind/pull/5466 for change
            // that made this necessary in 3.1 )
            @get:JsonUnwrapped var gallery: TestGallery
    )

    data class TestGalleryWidget_GOOD(val widgetReferenceId: String) {
        @JsonUnwrapped lateinit var gallery: TestGallery
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    data class TestGallery(
            val id: String? = null,
            val headline: String? = null,
            val intro: String? = null,
            val role: String? = null,
            val images: List<TestImage>? = null
    )

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    data class TestImage(
            val id: String? = null,
            val escenicId: String? = null,
            val caption: String? = null,
            val copyright: String? = null,
            val crops: Map<String, String>? = null
    )

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

    @Test
    fun serializes() {
        val result = defaultMapper.writeValueAsString(TestGalleryWidget_BAD("widgetReferenceId", gallery))
        assertEquals(validJson, result)
    }

    @Test
    fun deserializesSuccessful() {
        val obj = defaultMapper.readValue<TestGalleryWidget_BAD>(validJson)
        assertEquals("widgetReferenceId", obj.widgetReferenceId)
        assertEquals(gallery, obj.gallery)

    }

    @Test
    fun deserializesCorrectly() {
        defaultMapper.readValue<TestGalleryWidget_GOOD>(validJson)
    }
}
