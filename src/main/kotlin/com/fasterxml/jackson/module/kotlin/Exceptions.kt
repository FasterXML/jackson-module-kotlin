package com.fasterxml.jackson.module.kotlin

import com.fasterxml.jackson.databind.JsonMappingException
import kotlin.reflect.KParameter

/**
 * Specialized [JsonMappingException] sub-class used to indicate that a mandatory Kotlin constructor parameter was missing or null.
 */
class MissingKotlinParameterException(val parameter: KParameter, val msg: String) : JsonMappingException(null, msg)