package tools.jackson.module.kotlin;

import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * An interface to be inherited by JsonDeserializer that handles value classes that may wrap nullable.
 */
// To ensure maximum compatibility with StdDeserializer, this class is written in Java.
public abstract class WrapsNullableValueClassDeserializer<D> extends StdDeserializer<D> {
    protected WrapsNullableValueClassDeserializer(@NotNull KClass<?> vc) {
        super(JvmClassMappingKt.getJavaClass(vc));
    }

    protected WrapsNullableValueClassDeserializer(@NotNull Class<?> vc) {
        super(vc);
    }

    protected WrapsNullableValueClassDeserializer(@NotNull JavaType valueType) {
        super(valueType);
    }

    protected WrapsNullableValueClassDeserializer(@NotNull StdDeserializer<D> src) {
        super(src);
    }

    @Override
    @NotNull
    public final Class<D> handledType() {
        //noinspection unchecked
        return (Class<D>) super.handledType();
    }

    /**
     * If the parameter definition is a value class that wraps a nullable and is non-null,
     * and the input to JSON is explicitly null, this value is used.
     * Note that this will only be called from the KotlinValueInstantiator,
     * so it will not work for top-level deserialization of value classes.
     */
    // It is defined so that null can also be returned so that Nulls.SKIP can be applied.
    @Nullable
    public abstract D getBoxedNullValue();

    @Override
    public abstract D deserialize(@NotNull JsonParser p, @NotNull DeserializationContext ctxt) throws JacksonException;
}
