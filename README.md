[![Kotlin](https://img.shields.io/badge/kotlin-1.5.x-blue.svg)](https://kotlinlang.org) [![Kotlin Slack](https://img.shields.io/badge/chat-kotlin%20slack-orange.svg)](https://slack.kotlinlang.org/)

# Overview

Module that adds support for serialization/deserialization of [Kotlin](https://kotlinlang.org)
classes and data classes.
Previously a default constructor must have existed on the Kotlin object for Jackson to deserialize into the object.
With this module, single constructor classes can be used automatically,
and those with secondary constructors or static factories are also supported.

# Status

* release `2.13.4` (for Jackson `2.13.x`) [![GitHub Actions build](https://github.com/FasterXML/jackson-module-kotlin/actions/workflows/main.yml/badge.svg?branch=2.13)](https://github.com/FasterXML/jackson-module-kotlin/actions?query=branch%3A2.13)
* release `2.12.7` (for Jackson `2.12.x`) [![CircleCI](https://circleci.com/gh/FasterXML/jackson-module-kotlin/tree/2.12.svg?style=svg)](https://circleci.com/gh/FasterXML/jackson-module-kotlin/tree/2.12)
* release `2.11.4` (for Jackson `2.11.x`) [![CircleCI](https://circleci.com/gh/FasterXML/jackson-module-kotlin/tree/2.11.svg?style=svg)](https://circleci.com/gh/FasterXML/jackson-module-kotlin/tree/2.11)
* release `2.10.5` (for Jackson `2.10.x`)

Releases require that you have included Kotlin stdlib and reflect libraries already.

Gradle:
```
implementation "com.fasterxml.jackson.module:jackson-module-kotlin:2.13.+"
```

Maven:
```xml
<dependency>
    <groupId>com.fasterxml.jackson.module</groupId>
    <artifactId>jackson-module-kotlin</artifactId>
    <version>2.13.4</version>
</dependency>
```

# Usage

For any Kotlin class or data class constructor, the JSON property names will be inferred
from the parameters using Kotlin runtime type information.

To use, just register the Kotlin module with your ObjectMapper instance:

```kotlin
// With Jackson 2.12 and later
import tools.jackson.module.kotlin.jacksonObjectMapper
...
val mapper = jacksonObjectMapper()
// or
import tools.jackson.module.kotlin.registerKotlinModule
...
val mapper = ObjectMapper().registerKotlinModule()
// or
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule
...
val mapper = jsonMapper {
  addModule(kotlinModule())
}
```

<details>
  <summary>Jackson versions prior to 2.10–2.11</summary>

```kotlin
import com.fasterxml.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
...
val mapper = JsonMapper.builder().addModule(KotlinModule()).build()
```
</details>


<details>
  <summary>Jackson versions prior to 2.10</summary>

```kotlin
import com.fasterxml.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.KotlinModule
...
val mapper = ObjectMapper().registerModule(KotlinModule())
```
</details>

A simple data class example:
```kotlin
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue

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

Also, there are some convenient operator overloading extension functions for JsonNode inheritors.
```kotlin
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import tools.jackson.module.kotlin.*

// ...
val objectNode: ObjectNode = JsonNodeFactory.instance.objectNode()
objectNode.put("foo1", "bar").put("foo2", "baz").put("foo3", "bax")
objectNode -= "foo1"
objectNode -= listOf("foo2")
println(objectNode.toString()) // {"foo3":"bax"}

// ...
val arrayNode: ArrayNode = JsonNodeFactory.instance.arrayNode()
arrayNode += "foo"
arrayNode += true
arrayNode += 1
arrayNode += 1.0
arrayNode += "bar".toByteArray()
println(arrayNode.toString()) // ["foo",true,1,1.0,"YmFy"]
```

# Compatibility

(NOTE: incomplete! Please submit corrections/additions via PRs!)

Different `kotlin-core` versions are supported by different Jackson Kotlin module minor versions.
Here is an incomplete list of supported versions:

* Jackson 2.14.x: Kotlin-core 1.4 - 1.7
* Jackson 2.13.x: Kotlin-core 1.4 - 1.7

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
    * If you're still running into problems, you might also need to add a proguard keep rule for the specific classes you want to (de-)serialize. For example, if all your models are inside the package `com.example.models`, you could add the rule `-keep class com.example.models.** { *; }`
 
# Support for Kotlin Built-in classes

These Kotlin classes are supported with the following fields for serialization/deserialization
(and other fields are hidden that are not relevant):

* Pair _(first, second)_
* Triple _(first, second, third)_
* IntRange _(start, end)_
* CharRange _(start, end)_
* LongRange _(start, end)_

(others are likely to work, but may not be tuned for Jackson)

# Sealed classes without @JsonSubTypes
Subclasses can be detected automatically for sealed classes, since all possible subclasses are known
at compile-time to Kotlin. This makes `com.fasterxml.jackson.annotation.JsonSubTypes` redundant.
A `com.fasterxml.jackson.annotation.@JsonTypeInfo` annotation at the base-class is still necessary. 

```kotlin
  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
  sealed class SuperClass{
      class A: SuperClass()
      class B: SuperClass()
  }

...
val mapper = jacksonObjectMapper()
val root: SuperClass = mapper.readValue(json)
when(root){
    is A -> "It's A"
    is B -> "It's B"
}
```


# Configuration

The Kotlin module may be given a few configuration parameters at construction time;
see the [inline documentation](https://github.com/FasterXML/jackson-module-kotlin/blob/master/src/main/kotlin/tools/jackson/module/kotlin/KotlinModule.kt)
for details on what options are available and what they do.

```kotlin
val mapper = JsonMapper.builder()
        .addModule(KotlinModule(strictNullChecks = true))
        .build()
```

If your `ObjectMapper` is constructed in Java, there is a builder method
provided for configuring these options:

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
    * Dmitry Spikhalskiy (@Spikhalskiy) -- since 2.14
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

## Contributing

We welcome any contributions—reports of issues, ideas for enhancements, and pull requests related to either of those.

See the [main Jackson contribution guidlines](https://github.com/FasterXML/jackson/blob/master/CONTRIBUTING.md) for more details.

### Branches

If you are going to write code, choose the appropriate base branch:

- `2.12` for bugfixes against the current stable version
- `2.13` for additive functionality & features or [minor](https://semver.org), backwards compatible changes to existing behavior to be included in the next minor version release
- `master` for significant changes to existing behavior, which will be part of Jackson 3.0

### Failing tests

There are a number of tests for functionality that is broken, mostly in the [failing](https://github.com/FasterXML/jackson-module-kotlin/tree/master/src/test/kotlin/tools/jackson/module/kotlin/test/github/failing)
package but a few as part of other test suites.  Instead of ignoring these tests (with JUnit's `@Ignore` annotation)
or excluding them from being run as part of automated testing, the tests are written to demonstrate the failure
(either making a call that throws an exception or with an assertion that fails) but not fail the build, except if the
underlying issue is fixed.  This allows us to know when the tested functionality has been incidentally fixed by
unrelated code changes.

See the [tests readme](https://github.com/FasterXML/jackson-module-kotlin/tree/master/src/test/kotlin/tools/jackson/module/kotlin/README.md) for more information.
