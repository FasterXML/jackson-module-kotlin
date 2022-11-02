package tools.jackson.module.kotlin

import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.MapperFeature
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
    val valueParameters: List<KParameter> by lazy { callable.valueParameters }

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
