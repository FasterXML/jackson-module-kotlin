package com.fasterxml.jackson.module.kotlin

/**
 * Special handling for singletons.
 */
@Deprecated(
    level = DeprecationLevel.ERROR,
    message = "It will be removed in 2.19 to unify with KotlinFeature.",
    replaceWith = ReplaceWith("KotlinFeature.SingletonSupport")
)
enum class SingletonSupport {
    // No special handling of singletons (pre-2.10 behavior)
    //      Each time a Singleton object is deserialized a new instance is created.
    DISABLED,
    // Deserialize then canonicalize (was the default in 2.10)
    //      Deserializing a singleton overwrites the value of the single instance.
    //     [jackson-module-kotlin#225]: keep Kotlin singletons as singletons
    CANONICALIZE
}
