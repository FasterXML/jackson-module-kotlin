package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
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

class NullInputException(msg: String) : MismatchedInputException(null, msg)
