package tools.jackson.module.kotlin

import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.MapperFeature
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.valueParameters

/**
 * A class that abstracts the creation of instances by calling KFunction.
 * @see KotlinValueInstantiator
 */
internal sealed class ValueCreator<T> {
    /**
     * Function to be call.
     */
    protected abstract val callable: KFunction<T>

    /**
     * Initial value for accessibility by reflection.
     */
    protected abstract val accessible: Boolean

    /**
     * ValueParameters of the KFunction to be called.
     */
    // If this result is cached, it will coexist with the SoftReference managed value in kotlin-reflect,
    // and there is a risk of doubling the memory consumption, so it should not be cached.
    // @see #584
    val valueParameters: List<KParameter> get() = callable.valueParameters

/**
     * Checking process to see if access from context is possible.
     * @throws  IllegalAccessException
     */
    fun checkAccessibility(ctxt: DeserializationContext) {
        if ((!accessible && ctxt.config.isEnabled(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS)) ||
            (accessible && ctxt.config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS))) {
            return
        }

        throw IllegalAccessException("Cannot access to function or companion object instance, target: $callable")
    }

    /**
     * Function call with default values enabled.
     */
    fun callBy(args: Map<KParameter, Any?>): T = callable.callBy(args)
}
