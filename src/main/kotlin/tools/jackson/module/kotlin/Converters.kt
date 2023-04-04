package tools.jackson.module.kotlin

import tools.jackson.databind.ser.std.StdDelegatingSerializer
import tools.jackson.databind.util.StdConverter
import kotlin.reflect.KClass

// S is nullable because value corresponds to a nullable value class
// @see KotlinNamesAnnotationIntrospector.findNullSerializer
internal class ValueClassBoxConverter<S : Any?, D : Any>(
    unboxedClass: Class<S>,
    valueClass: KClass<D>
) : StdConverter<S, D>() {
    private val boxMethod = valueClass.java.getDeclaredMethod("box-impl", unboxedClass).apply {
        if (!this.isAccessible) this.isAccessible = true
    }

    @Suppress("UNCHECKED_CAST")
    override fun convert(value: S): D = boxMethod.invoke(null, value) as D

    val delegatingSerializer: StdDelegatingSerializer by lazy { StdDelegatingSerializer(this) }
}
