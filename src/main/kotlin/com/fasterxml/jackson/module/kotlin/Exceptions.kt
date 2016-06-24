package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.JsonMappingException
import java.io.Closeable
import kotlin.reflect.KParameter

/**
 * Specialized [JsonMappingException] sub-class used to indicate that a mandatory Kotlin constructor parameter was missing or null.
 */
class MissingKotlinParameterException(val parameter: KParameter, val processor: Closeable? = null, val msg: String) : JsonMappingException(processor, msg)