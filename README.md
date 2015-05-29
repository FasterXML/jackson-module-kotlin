# Overview

Module that adds support for serialization/deserialization of [Kotlin](http://kotlinlang.org) classes and data classes.  Previously a default constructor must have existed on the Kotlin object for Jackson to deserialize into the object.  With this module, single constructor classes can be used automatically, and those with secondary constructors or static factories are also supported.

# Status

[![Build Status](https://travis-ci.org/FasterXML/jackson-module-kotlin.svg)](https://travis-ci.org/FasterXML/jackson-module-kotlin)

Releases are available on Maven Central:

* Release 2.5.3 (Compatible with Kotlinb 0.11.91 M11 release and Jackson 2.5.x)
* Release 2.5.1 (compatible with Kotlin 0.10.4 [M10 release] and Jackson 2.5.x)
* Release 2.4.4-1 (compatible with Kotlin 0.10.4 [M10 release] and Jackson 2.4.x)
* Release 2.4.4 (compatible with Kotlin 0.9.66 [M9 release] and Jackson 2.4.x)

Gradle:
```
compile 'com.fasterxml.jackson.module:jackson-module-kotlin:2.5.3'
```

Maven:
```xml
<dependency>
    <groupId>com.fasterxml.jackson.module</groupId>
    <artifactId>jackson-module-kotlin</artifactId>
    <version>2.5.3</version>
</dependency>
```

# Usage

For any Kotlin class or data class constructor, the JSON property names will be inferred from the parameters using Kotlin runtime type information.

To use, just register the Kotlin module with your ObjectMapper instance:

```kotlin
val mapper = ObjectMapper().registerModule(KotlinModule())
```

or with the extension functions imported from `import com.fasterxml.jackson.module.kotlin.*`, one of:

```kotlin
val mapper = jacksonObjectMapper()
```

```kotlin
val mapper = ObjectMapper().registerKotlinModule()
```

A simple data class example:
```kotlin
data class MyStateObject(val name: String, val age: Int)

...
val mapper = jacksonObjectMapper()
val state = mapper.readValue(json, javaClass<MyStateObject>())

```

In Kotlin M10+ you do not need the javaClass parameter, it is inferred for all ObjectMapper functions that are possible (and a few on ObjectReader).  Therefore you can do one of:
```kotlin
import com.fasterxml.jackson.module.kotlin.*

val state = mapper.readValue<MyStateObject>(json)
// or
val state: MyStateObject = mapper.readValue(json)
// or
myMemberWithType = mapper.readValue(json)
```

# Annotations

You can intermix non-field values in the constructor and `JsonProperty` annotation in the constructor.  Any fields not present in the constructor will be set after the constructor call.  An example of these concepts:

```kotlin
   JsonInclude(JsonInclude.Include.NON_EMPTY)
   class StateObjectWithPartialFieldsInConstructor(val name: String, JsonProperty("age") val years: Int)    {
        JsonProperty("address") var primaryAddress: String = "" // does not have to be nullable
        var createdDt: DateTime by Delegates.notNull()
    }
```

Note that using Delegates.notNull() will ensure that the value is never null when read, while letting it be instantiated after the construction of the class.

# Caveats

* The `JsonCreator` annotation is optional unless you have more than one constructor that is valid, or you want to use a static factory method (which also must have `platformStatic` annotation).  In these cases, annotate only one method as `JsonCreator`.
* Currently we use parameter name information in Kotlin that is compatible with Kotlin M8 through M11
* Serializing a member or top-level Kotlin class that implements Iterator requires a workaround, see [Issue #4](https://github.com/FasterXML/jackson-module-kotlin/issues/4) for easy workarounds.
 
# Support for Kotlin Built-in classes

These Kotlin classes are supported with the following fields for serialization/deserialization (and other fields are hidden that are not relevant):

* Pair _(first, second)_
* Triple _(first, second, third)_
* IntRange _(start, end)_
* DoubleRange _(start, end)_
* CharRange _(start, end)_
* ByteRange _(start, end)_
* ShortRange _(start, end)_
* LongRange _(start, end)_
* FloatRange _(start, end)_

(others are likely to work, but may not be tuned for Jackson)
