package com.fasterxml.jackson.module.kotlin

/**
 * Identification class for synthetic constructor generated for default arguments and value classes.
 */
private val DEFAULT_CONSTRUCTOR_MARKER: Class<*> = try {
    Class.forName("kotlin.jvm.internal.DefaultConstructorMarker")
} catch (ex: ClassNotFoundException) {
    throw IllegalStateException(
        "DefaultConstructorMarker not on classpath. Make sure the Kotlin stdlib is on the classpath."
    )
}

val Class<*>.isKotlinDefaultConstructorMarker: Boolean
    get() = this == DEFAULT_CONSTRUCTOR_MARKER
