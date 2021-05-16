package com.fasterxml.jackson.module.kotlin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Wrapper to avoid costly calls using spread operator.
 * @since 2.13
 */
class SpreadWrapper {
    public static <T> Constructor<T> getConstructor(
            @NotNull Class<T> clazz,
            @NotNull Class<?>[] parameterTypes
    ) throws NoSuchMethodException {
        return clazz.getConstructor(parameterTypes);
    }

    public static <T> T newInstance(
            @NotNull Constructor<T> constructor,
            @NotNull Object[] initargs
    ) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return constructor.newInstance(initargs);
    }

    public static Method getDeclaredMethod(
            @NotNull Class<?> clazz,
            @NotNull String name,
            @NotNull Class<?>[] parameterTypes
    ) throws NoSuchMethodException {
        return clazz.getDeclaredMethod(name, parameterTypes);
    }

    /**
     * Instance is null on static method
     */
    public static Object invoke(
            @NotNull Method method,
            @Nullable Object instance,
            @NotNull Object[] args
    ) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(instance, args);
    }
}
