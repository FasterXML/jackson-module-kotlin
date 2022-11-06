package tools.jackson.module.kotlin

import tools.jackson.core.JsonParser
import tools.jackson.databind.exc.MismatchedInputException
import java.io.Closeable
import kotlin.reflect.KParameter

/**
 * Specialized [JsonMappingException] sub-class used to indicate that a mandatory Kotlin constructor
 * parameter was missing or null.
 */
class MissingKotlinParameterException(val parameter: KParameter,
                                      processor: JsonParser? = null,
                                      msg: String) : MismatchedInputException(processor, msg) {
    @Deprecated("Use main constructor", ReplaceWith("MissingKotlinParameterException(KParameter, JsonParser?, String)"))
    constructor(
            parameter: KParameter,
            processor: Closeable? = null,
            msg: String
    ) : this(parameter, processor as JsonParser, msg)
}
