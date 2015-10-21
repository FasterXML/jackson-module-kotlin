[![Kotlin](https://img.shields.io/badge/kotlin-1.0.0--beta--1038-blue.svg)](http://kotlinlang.org) [![Build Status](https://travis-ci.org/FasterXML/jackson-module-kotlin.svg)](https://travis-ci.org/FasterXML/jackson-module-kotlin) [![Kotlin Slack](https://img.shields.io/badge/chat-kotlin%20slack-orange.svg)](http://kotlinslackin.herokuapp.com)

# Overview

Module that adds support for serialization/deserialization of [Kotlin](http://kotlinlang.org) classes and data classes.  Previously a default constructor must have existed on the Kotlin object for Jackson to deserialize into the object.  With this module, single constructor classes can be used automatically, and those with secondary constructors or static factories are also supported.

# Status

Releases are available on Maven Central:

Kotlin 1.0.0 BETA
* release 2.6.3-1 (compatible with Kotlin ABI 1.0.0 and Jackson 2.6.x)

Kotlin M14
* release 2.6.2-3 (compatible with Kotlin 0.14.x M14 release and Jackson 2.6.x)

Kotlin M13
* Release 2.6.2-1 (Compatible with Kotlin 0.13.x M13 release and Jackson 2.6.x)

Kotlin M12
* Release 2.6.1 (Compatible wwith Kotlin 0.12.x M12 release and Jackson 2.6.x)
* Release 2.5.3.1 (Compatible with Kotlin 0.12.x M12 release and Jackson 2.5.x)

Releases require that you have included Kotlin stdlib and reflect libraries already.

(In M12+ of Kotlin, keep your constructors simple, if you have default values for parameters then alternatively generated constructors might cause Jackson to not be able to select the correct constructor.  Working on this for later releases.)


Gradle:
```
compile 'com.fasterxml.jackson.module:jackson-module-kotlin:2.6.2-2'
runtime "org.jetbrains.kotlin:kotlin-reflect:0.14.449"
```

Maven:
```xml
<dependency>
    <groupId>com.fasterxml.jackson.module</groupId>
    <artifactId>jackson-module-kotlin</artifactId>
    <version>2.6.3-1</version>
</dependency>
<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-reflect</artifactId>
    <version>1.0.0-beta-1038</version>
    <scope>runtime</scope>
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
* DoubleRange _(start, end)_
* CharRange _(start, end)_
* ByteRange _(start, end)_
* ShortRange _(start, end)_
* LongRange _(start, end)_
* FloatRange _(start, end)_

(others are likely to work, but may not be tuned for Jackson)
