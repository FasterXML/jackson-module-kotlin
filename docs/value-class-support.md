`jackson-module-kotlin` supports many use cases of `value class` (`inline class`).  
This page summarizes the basic policy and points to note regarding the use of the `value class`.

For technical details on `value class` handling, please see [here](./value-class-handling.md).

# Note on the use of `value class`
`jackson-module-kotlin` supports the `value class` for many common use cases, both serialization and deserialization.  
However, full compatibility with normal classes (e.g. `data class`) is not achieved.  
In particular, there are many edge cases for the `value class` that wraps nullable.

The cause of this difference is that the `value class` itself and the functions that use the `value class` are
compiled into bytecodes that differ significantly from the normal classes.  
Due to this difference, some cases cannot be handled by basic `Jackson` parsing, which assumes `Java`.  
Known issues related to `value class` can be found [here](https://github.com/FasterXML/jackson-module-kotlin/issues?q=is%3Aissue+is%3Aopen+label%3A%22value+class%22).

In addition, one of the features of the `value class` is improved performance,
but when using `Jackson` (not only `Jackson`, but also other libraries that use reflection),
the performance is rather reduced.  
This can be confirmed from [kogera-benchmark](https://github.com/ProjectMapK/kogera-benchmark?tab=readme-ov-file#comparison-of-normal-class-and-value-class).

For these reasons, we recommend careful consideration when using `value class`.

# Basic handling of `value class`
A `value class` is basically treated like a value.

For example, the serialization of `value class` is as follows

```kotlin
@JvmInline
value class Value(val value: Int)

val mapper = jacksonObjectMapper()
mapper.writeValueAsString(Value(1)) // -> 1
```

This is different from the `data class` serialization result.

```kotlin
data class Data(val value: Int)

mapper.writeValueAsString(Data(1)) // -> {"value":1}
```

The same policy applies to deserialization.

This policy was decided with reference to the behavior as of `jackson-module-kotlin 2.14.1` and [kotlinx-serialization](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/value-classes.md#serializable-value-classes).  
However, these are just basic policies, and the behavior can be overridden with `JsonSerializer` or `JsonDeserializer`.

# Notes on customization
As noted above, the content associated with the `value class` is not fully compatible with the normal class.  
Here is a summary of the customization considerations for such contents.

## Annotation
Annotations assigned to parameters in a primary constructor that contains `value class` as a parameter will not work.
It must be assigned to a field or getter.

```kotlin
data class Dto(
    @JsonProperty("vc") // does not work
    val p1: ValueClass,
    @field:JsonProperty("vc") // does work
    val p2: ValueClass
)
```

See #651 for details.

## On serialize
### JsonValue
The `JsonValue` annotation is supported.

```kotlin
@JvmInline
value class ValueClass(val value: UUID) {
    @get:JsonValue
    val jsonValue get() = value.toString().filter { it != '-' }
}

// -> "e5541a61ac934eff93516eec0f42221e"
mapper.writeValueAsString(ValueClass(UUID.randomUUID()))
```

### JsonSerializer
The `JsonSerializer` basically supports the following methods:
registering to `ObjectMapper`, giving the `JsonSerialize` annotation.  
Also, although `value class` is basically serialized as a value,
but it is possible to serialize `value class` like an object by using `JsonSerializer`.

```kotlin
@JvmInline
value class ValueClass(val value: UUID)

class Serializer : StdSerializer<ValueClass>(ValueClass::class.java) {
    override fun serialize(value: ValueClass, gen: JsonGenerator, provider: SerializerProvider) {
        val uuid = value.value
        val obj = mapOf(
            "mostSignificantBits" to uuid.mostSignificantBits,
            "leastSignificantBits" to uuid.leastSignificantBits
        )

        gen.writeObject(obj)
    }
}

data class Dto(
    @field:JsonSerialize(using = Serializer::class)
    val value: ValueClass
)

// -> {"value":{"mostSignificantBits":-6594847211741032479,"leastSignificantBits":-5053830536872902344}}
mapper.writeValueAsString(Dto(ValueClass(UUID.randomUUID())))
```

Note that specification with the `JsonSerialize` annotation will not work
if the `value class` wraps null and the property definition is non-null.

## On deserialize
### JsonDeserializer
Like `JsonSerializer`, `JsonDeserializer` is basically supported.  
However, it is recommended that `WrapsNullableValueClassDeserializer` be inherited and implemented as a
deserializer for `value class` that wraps nullable.

This deserializer is intended to make the deserialization result be a wrapped null if the parameter definition
is a `value class` that wraps nullable and non-null, and the value on the `JSON` is null.  
An example implementation is shown below.

```kotlin
@JvmInline
value class ValueClass(val value: String?)

class Deserializer : WrapsNullableValueClassDeserializer<ValueClass>(ValueClass::class) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ValueClass {
        TODO("Not yet implemented")
    }

    override fun getBoxedNullValue(): ValueClass = WRAPPED_NULL

    companion object {
        private val WRAPPED_NULL = ValueClass(null)
    }
}
```

### JsonCreator
`JsonCreator` basically behaves like a `DELEGATING` mode.  
Note that defining a creator with multiple arguments will result in a runtime error.

As a workaround, a factory function defined in bytecode with a return value of `value class` can be deserialized in the same way as a normal creator.

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
