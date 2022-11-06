package tools.jackson.module.kotlin.test.github

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import org.junit.Test

//TODO : Fix @JsonIgnore
class TestGithub124 {
    class NonSerializable(private val field: Any?) {
        override fun toString() = "NonSerializable"
    }

    data class Foo @JsonCreator constructor(
        @JsonProperty("name") val name: String,
        @JsonProperty("query") val rawQuery: String)


    @Test
    fun test() {
        val objMapper = jacksonObjectMapper()

        val deserialized: Foo = objMapper.readValue("""{"name": "foo", "query": "bar"}""")
        val serialized = objMapper.writeValueAsString(deserialized)

        assert(serialized == """{"name":"foo","query":"bar"}""")
    }
}