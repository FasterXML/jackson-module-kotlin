[![Kotlin](https://img.shields.io/badge/kotlin-1.0.0-blue.svg)](http://kotlinlang.org) [![Build Status](https://travis-ci.org/FasterXML/jackson-module-kotlin.svg)](https://travis-ci.org/FasterXML/jackson-module-kotlin) [![Kotlin Slack](https://img.shields.io/badge/chat-kotlin%20slack-orange.svg)](http://kotlinslackin.herokuapp.com)

# Overview

Module that adds support for serialization/deserialization of [Kotlin](http://kotlinlang.org) classes and data classes.  Previously a default constructor must have existed on the Kotlin object for Jackson to deserialize into the object.  With this module, single constructor classes can be used automatically, and those with secondary constructors or static factories are also supported.

# Status

Older versions of the Jackson-Kotlin module are not compatible with Kotlin 1.0.0.  You must update.  Releases for Kotlin 1.0.0 will be available on Maven Central shortly.  In the meantime use the EAP repository:

```
maven {
   url  "http://dl.bintray.com/jaysonminard/kohesive"
}
```

For Kotlin 1.0.0, use one of:

* release `2.7.1-1` (for Jackson `2.7.x`)
* release `2.6.5-2` (for Jackson `2.6.x`)
* release `2.5.5-2` (for Jackson `2.5.x`)

Releases require that you have included Kotlin stdlib and reflect libraries already.


Gradle:
```
compile "com.fasterxml.jackson.module:jackson-module-kotlin:2.6.4"
```

Maven:
```xml
<dependency>
    <groupId>com.fasterxml.jackson.module</groupId>
    <artifactId>jackson-module-kotlin</artifactId>
    <version>2.6.4</version>
</dependency>
```

# KNOWN PROBLEMS

In M12+ of Kotlin, keep your constructors simple, if you have default values for parameters then alternatively generated constructors might cause Jackson to not be able to select the correct constructor.  Working on this for later releases.

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
import com.fasterxml.jackson.module.kotlin.*

data class MyStateObject(val name: String, val age: Int)

...
val mapper = jacksonObjectMapper()

val state = mapper.readValue<MyStateObject>(json)
// or
val state: MyStateObject = mapper.readValue(json)
// or
myMemberWithType = mapper.readValue(json)
```

With `2.6.3-2` or newer of the module all inferred types for the extension functions carry in full generic information (reified generics).
Therefore using `readValue()` extension without the `Class` parameter will reify the type and automatically create a `TypeReference` for Jackson.

# Annotations

You can intermix non-field values in the constructor and `JsonProperty` annotation in the constructor.  Any fields not present in the constructor will be set after the constructor call and therefore must be nullable with default value.  An example of these concepts:

```kotlin
   class StateObjectWithPartialFieldsInConstructor(val name: String, JsonProperty("age") val years: Int)    {
        JsonProperty("address") var primaryAddress: String? = null
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
* CharRange _(start, end)_
* LongRange _(start, end)_

(others are likely to work, but may not be tuned for Jackson)
