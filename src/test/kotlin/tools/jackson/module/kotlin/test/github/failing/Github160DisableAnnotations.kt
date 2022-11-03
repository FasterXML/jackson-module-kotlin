package tools.jackson.module.kotlin.test.github.failing

import tools.jackson.databind.MapperFeature
import tools.jackson.databind.exc.MismatchedInputException
import tools.jackson.module.kotlin.jacksonMapperBuilder
import tools.jackson.module.kotlin.readValue
import tools.jackson.module.kotlin.test.expectFailure
import org.junit.Test

class TestGithub160 {
    data class DataClass(val blah: String)

    @Test
    fun dataClass() {
        val mapper = _root_ide_package_.tools.jackson.module.kotlin.jacksonMapperBuilder()
            .configure(MapperFeature.USE_ANNOTATIONS, false)
            .build()
        expectFailure<MismatchedInputException>("GitHub #160 has been fixed!") {
            mapper.readValue<DataClass>("""{"blah":"blah"}""")
        }
    }
}
