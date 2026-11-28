package tools.jackson.module.kotlin.test.github

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.deser.DeserializationProblemHandler
import tools.jackson.module.kotlin.KotlinInvalidNullException
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule
import tools.jackson.module.kotlin.readValue
import kotlin.test.assertEquals

private data class Dto(val foo: String)

// Recovers an illegal `null` for a `String` creator parameter with a fixed value.
private class StringNullRecoveryHandler : DeserializationProblemHandler() {
    override fun handleInstantiationProblem(
        ctxt: DeserializationContext,
        instClass: Class<*>,
        argument: Any?,
        t: Throwable
    ): Any = if (instClass == String::class.java && argument == null) {
        "recovered"
    } else {
        NOT_HANDLED
    }
}

// A handler that recovers nothing; all methods keep the `NOT_HANDLED` default.
private class NoRecoveryHandler : DeserializationProblemHandler()

class Github875 {
    // https://github.com/FasterXML/jackson-module-kotlin/issues/875
    @Test
    fun missingRequiredParameterIsRecoverableByProblemHandler() {
        val mapper = jsonMapper {
            addModule(kotlinModule())
            addHandler(StringNullRecoveryHandler())
        }

        assertEquals(Dto("recovered"), mapper.readValue<Dto>("{}"))
    }

    @Test
    fun explicitNullForRequiredParameterIsRecoverableByProblemHandler() {
        val mapper = jsonMapper {
            addModule(kotlinModule())
            addHandler(StringNullRecoveryHandler())
        }

        assertEquals(Dto("recovered"), mapper.readValue<Dto>("""{"foo":null}"""))
    }

    @Test
    fun unhandledIllegalNullStillThrowsKotlinInvalidNullException() {
        val mapper = jsonMapper {
            addModule(kotlinModule())
            addHandler(NoRecoveryHandler())
        }

        val ex = assertThrows<KotlinInvalidNullException> { mapper.readValue<Dto>("{}") }

        assertEquals("foo", ex.kotlinPropertyName)
        assertEquals(Dto::class, ex.targetType.kotlin)
    }
}
