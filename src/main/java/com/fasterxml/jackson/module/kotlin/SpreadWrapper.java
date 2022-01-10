package com.fasterxml.jackson.module.kotlin;

import kotlin.reflect.KFunction;
import org.jetbrains.annotations.NotNull;

/**
 * Wrapper to avoid costly calls using spread operator.
 * @since 2.13
 */
class SpreadWrapper {
    private SpreadWrapper() {}

    static <T> T call(@NotNull KFunction<T> function, @NotNull Object[] args) {
        return function.call(args);
    }
}
