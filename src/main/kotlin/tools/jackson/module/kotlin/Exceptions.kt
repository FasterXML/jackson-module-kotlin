package tools.jackson.module.kotlin

import tools.jackson.core.JsonParser
import tools.jackson.databind.exc.MismatchedInputException
import java.io.Closeable
import kotlin.reflect.KParameter

/**
 * Specialized [JsonMappingException] sub-class used to indicate that a mandatory Kotlin constructor
 * parameter was missing or null.
 */
@Deprecated(
    "It will be removed in jackson-module-kotlin 2.16. See #617 for details.",
    ReplaceWith(
        "MismatchedInputException",
        "com.fasterxml.jackson.databind.exc.MismatchedInputException"
    ),
    DeprecationLevel.WARNING
)
// When deserialized by the JDK, the parameter property will be null, ignoring nullability.
// This is a temporary workaround for #572 and we will eventually remove this class.
class MissingKotlinParameterException(@Transient val parameter: KParameter,
                                      processor: JsonParser? = null,
                                      msg: String) : MismatchedInputException(processor, msg) {
    @Deprecated("Use main constructor", ReplaceWith("MissingKotlinParameterException(KParameter, JsonParser?, String)"))
    constructor(
            parameter: KParameter,
            processor: Closeable? = null,
            msg: String
    ) : this(parameter, processor as JsonParser, msg)
}
