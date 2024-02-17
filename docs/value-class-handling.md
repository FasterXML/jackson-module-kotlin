This is a document that summarizes how `value class` is handled in `kotlin-module`.

# Annotation assigned to a property (parameter)
In `Kotlin`, annotations on properties will be assigned to the parameters of the primary constructor.  
On the other hand, if the parameter contains a `value class`, this annotation will not work.  
See #651 for details.

# Serialize
Serialization is performed as follows

1. If the value is unboxed in the getter of a property, re-box it
2. Serialization is performed by the serializer specified for the class or by the default serializer of `kotlin-module`

## Re-boxing of value
Re-boxing is handled by `KotlinAnnotationIntrospector#findSerializationConverter`.

The properties re-boxed here are handled as if the type of the getter was `value class`.  
This allows the `JsonSerializer` specified for the mapper, class and property to work.

### Edge case on `value class` that wraps `null`
If the property is non-null and the `value class` that is the value wraps `null`,
then the value is re-boxed by `KotlinAnnotationIntrospector#findNullSerializer`.  
This is the case for serializing `Dto` as follows.

```kotlin
@JvmInline
value class WrapsNullable(val v: String?)

data class Dto(val value: WrapsNullable = WrapsNullable(null))
```

In this case, features like the `JsonSerialize` annotation will not work as expected due to the difference in processing paths.

## Default serializers with `kotlin-module`
Default serializers for boxed values are implemented in `KotlinSerializers`.  
There are two types: `ValueClassUnboxSerializer` and `ValueClassSerializer.StaticJsonValue`.

The former gets the value by unboxing and the latter by executing the method with the `JsonValue` annotation.  
The serializer for the retrieved value is then obtained and serialization is performed.

# Deserialize
Deserialization is performed as follows

1. Get `KFunction` from a non-synthetic constructor (if the constructor is a creator)
2. If it is unboxed on a parameter, refine it to a boxed type
3. `value class` is deserialized by `Jackson` default handling or by `kotlin-module` deserializer
4. Instantiation is done by calling `KFunction`

The special `JsonDeserializer`, `WrapsNullableValueClassDeserializer`, is described in the [section on instantiation](#Instantiation).

## Get `KFunction` from non-synthetic constructor
Constructor with `value class` parameters compiles into a `private` non-synthesized constructor and a synthesized constructor.

A `KFunction` is inherently interconvertible with any constructor or method in a `Java` reflection.  
In the case of a constructor with a `value class` parameter, it is the synthetic constructor that is interconvertible.

On the other hand, `Jackson` does not handle synthetic constructors.  
Therefore, `kotlin-module` needs to get `KFunction` from a `private` non-synthetic constructor.

This acquisition process is implemented as a `valueClassAwareKotlinFunction` in `ReflectionCache.kt`.

## Refinement to boxed type
Refinement to a boxed type is handled by `KotlineNamesAnnotationIntrospector#refineDeserializationType`.  
Like serialization, the parameters refined here are handled as if the type of the parameter was `value class`.

This will cause the result of reading from the `PropertyValueBuffer` with `ValueInstantiator#createFromObjectWith` to be the boxed value.

## Deserialization of `value class`
Deserialization of `value class` may be handled by default by `Jackson` or by `kotlin-module`.

### by `Jackson`
If a custom `JsonDeserializer` is set or a special `JsonCreator` is defined,
deserialization of the `value class` is handled by `Jackson` just like a normal class.  
The special `JsonCreator` is a factory function that is configured to return the `value class` in bytecode.

The special `JsonCreator` is handled in exactly the same way as a regular class.  
That is, it does not have the restrictions that the mode is fixed to `DELEGATING`
or that it cannot have multiple arguments.  
This can be defined by setting the return value to `nullable`, for example

```kotlin
@JvmInline
value class PrimitiveMultiParamCreator(val value: Int) {
    companion object {
        @JvmStatic
        @JsonCreator
        fun creator(first: Int, second: Int): PrimitiveMultiParamCreator? =
            PrimitiveMultiParamCreator(first + second)
    }
}
```

### by `kotlin-module`
Deserialization using constructors or factory functions that return unboxed value in bytecode
is handled by the `WrapsNullableValueClassBoxDeserializer` that defined in `KotlinDeserializer.kt`.  

They must always have a parameter size of 1, like `JsonCreator` with `DELEGATING` mode specified.  
Note that the `kotlin-module` proprietary implementation raises an `InvalidDefinitionException`
if the parameter size is greater than 2.

## Instantiation
Instantiation by calling `KFunction` obtained from a constructor or factory function is done with `KotlinValueInstantiator#createFromObjectWith`.

Boxed values are required as `KFunction` arguments, but since the `value class` is read as a boxed value as described above,
basic processing is performed as in a normal class.
However, there is special processing for the edge case described below.

### Edge case on `value class` that wraps nullable
If the parameter type is `value class` and non-null, which wraps nullable, and the value on the JSON is null,
the wrapped null is expected to be read as the value.

```kotlin
@JvmInline
value class WrapsNullable(val value: String?)

data class Dto(val wrapsNullable: WrapsNullable)

val mapper = jacksonObjectMapper()

// serialized: {"wrapsNullable":null}
val json = mapper.writeValueAsString(Dto(WrapsNullable(null)))
// expected: Dto(wrapsNullable=WrapsNullable(value=null))
val deserialized = mapper.readValue<Dto>(json)
```

In `kotlin-module`, a special `JsonDeserializer` named `WrapsNullableValueClassDeserializer` was introduced to support this.  
This deserializer has a `boxedNullValue` property,
which is referenced in `KotlinValueInstantiator#createFromObjectWith` as appropriate.

I considered implementing it with the traditional `JsonDeserializer#getNullValue`,
but I chose to implement it as a special property because of inconsistencies that could not be resolved
if all cases were covered in detail in the prototype.  
Note that this property is referenced by `KotlinValueInstantiator#createFromObjectWith`,
so it will not work when deserializing directly.
