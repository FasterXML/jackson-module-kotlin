package tools.jackson.module.kotlin.test.github

import tools.jackson.databind.annotation.JsonDeserialize
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

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

        val original = SampleContainer(SampleImpl.One)

        val json = mapper.writeValueAsString(original)
//        println(json)
        val obj = mapper.readValue<SampleContainer>(json)
        assertEquals(original, obj)
    }
}
