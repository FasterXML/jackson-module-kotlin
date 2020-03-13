package com.fasterxml.jackson.module.kotlin

/**
 * Special handling for singletons.
 */
enum class SingletonSupport {
    // No special handling of singletons (pre-2.10 behavior)
    DISABLED,
    // Deserialize then canonicalize (was the default in 2.10)
    //     [jackson-module-kotlin#225]: keep Kotlin singletons as singletons
    CANONICALIZE
}
