package tools.jackson.module.kotlin.test.github.failing

import tools.jackson.module.kotlin.KotlinFeature.SingletonSupport
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule
import tools.jackson.module.kotlin.readValue
import tools.jackson.module.kotlin.test.expectFailure
import kotlin.test.assertSame
import org.junit.jupiter.api.Test

/**
 * An empty object should be deserialized as *the* Unit instance for a nullable Unit reference Type.
 */
class TestGithub518 {

    /**
     * Empty object did not serialize to the singleton Unit before 2.13 as described in
     * https://github.com/FasterXML/jackson-module-kotlin/issues/196.
     */
    @Test
    fun deserializeEmptyObjectToSingletonUnit() {
        assertSame(jacksonObjectMapper().readValue<Unit>("{}"), Unit)
    }

    /**
     * Empty object does not serialize to the singleton Unit for a nullable reference Type as described in
     * https://github.com/FasterXML/jackson-module-kotlin/issues/518.
     */
    @Test
    fun deserializeEmptyObjectToSingletonUnitFails() {
        expectFailure<AssertionError>("GitHub #518 has been fixed!") {
            assertSame(jacksonObjectMapper().readValue<Unit?>("{}"), Unit)
        }
    }

    /**
     * Empty object serializes to the singleton Unit for a nullable reference if singleton support is enabled. Is this
     * setting really required to deserialize Unit correctly or should it also work correctly without singleton support
     * enabled?
     */
    @Test
    fun deserializeEmptyObjectToSingletonUnitWithSingletonSupport() {
        val objectMapper = jsonMapper {
            addModule(kotlinModule {
                configure(
                    SingletonSupport,
                    true
                )
            })
        }
        assertSame(objectMapper.readValue<Unit?>("{}"), Unit)
    }
}
