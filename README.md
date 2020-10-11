[![Kotlin](https://img.shields.io/badge/kotlin-1.3.x-blue.svg)](http://kotlinlang.org) [![CircleCI](https://circleci.com/gh/FasterXML/jackson-module-kotlin.svg?style=svg)](https://circleci.com/gh/FasterXML/jackson-module-kotlin) [![Kotlin Slack](https://img.shields.io/badge/chat-kotlin%20slack-orange.svg)](http://slack.kotlinlang.org/)

# Overview

Module that adds support for serialization/deserialization of [Kotlin](http://kotlinlang.org) classes and data classes.
Previously a default constructor must have existed on the Kotlin object for Jackson to deserialize into the object.
With this module, single constructor classes can be used automatically, and those with secondary constructors or static factories are also supported.

# Status

2.9.8+ Releases are compiled with Kotlin 1.3.x, other older releases are Kotlin 1.2.x.  All should be compatible with
current Kotlin if you also ensure the `kotlin-reflect` dependency is included with the same version number as stdlib.

* release `2.11.3` (for Jackson `2.11.x`) [![CircleCI](https://circleci.com/gh/FasterXML/jackson-module-kotlin/tree/2.11.svg?style=svg)](https://circleci.com/gh/FasterXML/jackson-module-kotlin/tree/2.11)
* release `2.10.5` (for Jackson `2.10.x`)
* release `2.9.10` (for Jackson `2.9.x`)

Releases require that you have included Kotlin stdlib and reflect libraries already.

Gradle:
```
implementation "com.fasterxml.jackson.module:jackson-module-kotlin:2.11.+"
```

Maven:
```xml
<dependency>
    <groupId>com.fasterxml.jackson.module</groupId>
    <artifactId>jackson-module-kotlin</artifactId>
    <version>2.11.3</version>
</dependency>
```

# Usage

For any Kotlin class or data class constructor, the JSON property names will be inferred from the parameters using Kotlin runtime type information.

To use, just register the Kotlin module with your ObjectMapper instance:

```kotlin
val mapper = ObjectMapper().registerModule(KotlinModule())
// or with 2.10 and later
val mapper = JsonMapper.builder().addModule(KotlinModule()).build()
// or with 2.12 and later
val mapper = jsonMapper {
  addModule(kotlinModule())
}

```

or with the extension functions imported from `import com.fasterxml.jackson.module.kotlin.*`, one of:

```kotlin
val mapper = jacksonObjectMapper()
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

All inferred types for the extension functions carry in full generic information (reified generics).
Therefore, using `readValue()` extension without the `Class` parameter will reify the type and automatically create a `TypeReference` for Jackson.

# Annotations

You can intermix non-field values in the constructor and `JsonProperty` annotation in the constructor.
Any fields not present in the constructor will be set after the constructor call.
An example of these concepts:

```kotlin
   @JsonInclude(JsonInclude.Include.NON_EMPTY)
   class StateObjectWithPartialFieldsInConstructor(val name: String, @JsonProperty("age") val years: Int)    {
        @JsonProperty("address") lateinit var primaryAddress: String   // set after construction
        var createdDt: DateTime by Delegates.notNull()                // set after construction
        var neverSetProperty: String? = null                          // not in JSON so must be nullable with default
    }
```

Note that using `lateinit` or `Delegates.notNull()` will ensure that the value is never `null` when read, while letting it be instantiated after the construction of the class.

# Caveats

* The `@JsonCreator` annotation is optional unless you have more than one constructor that is valid, or you want to use a static factory method (which also must have `platformStatic` annotation, e.g. `@JvmStatic`).  In these cases, annotate only one method as `JsonCreator`.
* Serializing a member or top-level Kotlin class that implements Iterator requires a workaround, see [Issue #4](https://github.com/FasterXML/jackson-module-kotlin/issues/4) for easy workarounds.
* If using proguard:
  * `kotlin.Metadata` annotations may be stripped, preventing deserialization. Add a proguard rule to keep the `kotlin.Metadata` class: `-keep class kotlin.Metadata { *; }`
  * If you're getting `java.lang.ExceptionInInitializerError`, you may also need: `-keep class kotlin.reflect.** { *; }`
 
# Support for Kotlin Built-in classes

These Kotlin classes are supported with the following fields for serialization/deserialization (and other fields are hidden that are not relevant):

* Pair _(first, second)_
* Triple _(first, second, third)_
* IntRange _(start, end)_
* CharRange _(start, end)_
* LongRange _(start, end)_

(others are likely to work, but may not be tuned for Jackson)

# Configuration

The Kotlin module may be given a few configuration parameters at construction time; see the [inline documentation](https://github.com/FasterXML/jackson-module-kotlin/blob/master/src/main/kotlin/com/fasterxml/jackson/module/kotlin/KotlinModule.kt) for details on what options are available and what they do.

```kotlin
val mapper = JsonMapper.builder()
        .addModule(KotlinModule(strictNullChecks = true))
        .build()

// Or, from version 2.12
val mapper = jsonMapper {
    addModule(kotlinModule {
        strictNullChecks(true)
    })
}
```

If your `ObjectMapper` is constructed in Java, there is a builder method provided for configuring these options:

```java
KotlinModule kotlinModule = new KotlinModule.Builder()
        .strictNullChecks(true)
        .build();
ObjectMapper objectMapper = JsonMapper.builder()
        .addModule(kotlinModule)
        .build();
```

# Development

## Maintainers

Following developers have committer access to this project.

* Author: Jayson Minard (@apatrida) wrote this module; still helps issues from time to time
* Active Maintainers:
    * Drew Stephens (@dinomite)
    * Vyacheslav Artemyev (@viartemev)
* Co-maintainers:
    * Tatu Saloranta (@cowtowncoder)

You may at-reference them as necessary but please keep in mind that all
maintenance work is strictly voluntary (no one gets paid to work on this
or any other Jackson components) so there is no guarantee for timeliness of
responses.

All Pull Requests should be reviewed by at least one of active maintainers;
bigger architectural/design questions should be agreed upon by majority of
active maintainers (at this point meaning both Drew and Vyacheslav :) ).

## Releases & Branches

This module follows the release schedule of the rest of Jackson—the current version is consistent
across all Jackson components & modules. See the [jackson-databind README](https://github.com/FasterXML/jackson#actively-developed-versions) for details.
