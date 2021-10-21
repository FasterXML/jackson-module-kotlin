Project: jackson-module-kotlin

Active Maintainers:

* Drew Stephens (@dinomite)
* Vyacheslav Artemyev (@viartemev)

Co-maintainers:

* Jayson Minard (@apatrida) -- original Module Author, helps from time to time
* Tatu Saloranta (@cowtowncoder) -- at-large Jackson Author, helps from time to time

------------------------------------------------------------------------
=== Releases ===
------------------------------------------------------------------------

2.14.0 (not yet released)

No changes since 2.13

2.13.0 (30-Sep-2021)

#438: Fixed mapping failure when `private` `companion object` is named
 (reported, fix contributed by k163377@github)

2.12.5 (27-Aug-2021)
2.12.4 (06-Jul-2021)
2.12.3 (12-Apr-2021)

No changes since 2.12.2

2.12.2 (03-Mar-2021)

#409: `module-info.java` missing "exports"
 (reported by Elisha P)
#182: Nullable unsigned numbers do not serialize correctly
 (reported by bholzman@github)
 (fix contributed by Eric F)

2.12.1 (08-Jan-2021)

#402: Remove implicitly-included `java.base` dep in `module-info.java`
 (reported by UkonnRa@github)

2.12.0 (29-Nov-2020)

#322: Added extension methods to SimpleModule addSerializer and addDeserializer to support KClass arguments
    that register the serializer/deserializer for both the java type and java class.
#356: Kotlin 1.4 support
#385: Add Moditect, source module info, to allow Kotlin module usage with Java Module system
- Add Gradle Module Metadata (https://blog.gradle.org/alignment-with-gradle-module-metadata)

2.11.4 (12-Dec-2020)
2.11.3 (02-Oct-2020)
2.11.2 (02-Aug-2020)

No changes since 2.11.1

2.11.1 (25-Jun-2020)

#330: Kotlin version from 1.3.61 to 1.3.72

2.11.0 (26-Apr-2020)

#281: Hide singleton deserialization support behind a setting on the module,
    `singletonSupport` and enum `SingletonSupport`.  Defaults to pre-2.10 behavior.
#284: Use `AnnotationIntrospector.findRenameByField()` to support "is properties"
#321: Make MissingKotlinParameterException extend MismatchedInputException
- Add Builder for KotlinModule
- Kotlin updated to 1.3.61

2.10.5 (21-Jul-2020)

No changes since 2.10.4

2.10.4 (04-May-2020)

#330: Kotlin version from 1.3.61 to 1.3.72

2.10.3 (03-Mar-2020)

No changes since 2.10.2

2.10.2 (05-Jan-2020)

#270: 2.10.1 seems to output JSON field where name of function matches
  name of private field
 (reported by daviddenton@github)
#279: 2.10 introduces another binary compatibility issue in `KotlinModule`
  constructor
 (reported by Patrick S, fix contributed by Vladimir P)

2.10.1 (10-Nov-2019)

#80: Boolean property name starting with 'is' not serialized/deserialized properly
 (fix contributed by Andrey L)
#130: Using Kotlin Default Parameter Values when JSON value is null and Kotlin parameter
  type is Non-Nullable
 (fix contributed by NumezmaT@github)
#176: Version 2.9.7 breaks compatibility with Android minSdk < 24
 (reported jurriaan@github, fix submitted by Stéphane B)
#225: Don't instantiate new instances of Kotlin singleton objects
 (reported by Dico200@github; fix by Alain L)
- Make byte code target 1.8 (can't do many things with 1.7 anyway)
#254: Serializer/Deserializers for Sequences
 (reported by SprocketNYC@github; fix by Konstantin V)
#180: Handle nullable method parameters correctly (for creator methods)
 (reported and fixed by Laimiux@github)

Kotlin updated to 1.3.50

2.10.0 (26-Sep-2019)

#239: Auto-detect sealed classes (similar to `@JsonSubTypes`)
 (suggested, contributed impl by shartte@github)
* Kotlin 1.3.10 -> 1.3.41

2.9.10 (21-Sep-2019)
2.9.9 (16-May-2019)
2.9.8 (15-Dec-2018)

No known updates since 2.9.7

2.9.7 (19-Sep-2018)

- Kotlin 1.2.51
- no longer fail on types that do not have nullability information (i.e. generic type parameters as property types)
- Fixes #162 where in future Jackson the Kotlin module will fail to register if data binding is misconfigured
- Fixes #145 and #131 with better single string constructor detection
- Fixes #137 allowing nullable types for `ObjectMapper` extension functions such as `readValue`
- `JsonCreator` methods in companion objects can now use default parameter values
- Fixes #167 where local types made from SAM interfaces would previously throw an `UnsupportedOperationException`
- Fixes #168 where `JsonProperty(required=true)` was being ignored and overridden by nullability check

2.9.6 (12-Jun-2018)

No direct changes since 2.9.5 -- but `jackson-databind` has tons!

2.9.5 (26-Mar-2018)

- Kotlin 1.2(.21)!

2.9.4 (24-Jan-2018)

No changes since 2.9.3

2.9.3 (09-Dec-2017)

#94: Update kotlin version to 1.1.51

2.9.2 (14-Oct-2017)
2.9.1 (07-Sep-2017)
2.9.0 (30-Jul-2017)

(please fill me)

2.8.11 (24-Dec-2017)

Update Kotlin dep to 1.1.61

2.8.10 (24-Aug-2017)
2.8.9 (12-Jun-2017)
2.8.8 (05-Apr-2017)
2.8.7 (21-Feb-2017)
2.8.6 (12-Jan-2017)
2.8.5 (14-Nov-2016)
2.8.4 (14-Oct-2016)
2.8.3 (17-Sep-2016)
2.8.2 (30-Aug-2016)
2.8.1 (20-Jul-2016)

No changes since 2.8.0.

2.8.0 (04-Jul-2016)

#26: Default values for primitive parameters
#29: Problems deserializing object when default values for constructor parameters are used
- Update to Kotlin 1.0.2
- Added checks explicitly for nullable values being used in constructor or creator static methods
  to not allow NULL values into non-nullable types

2.7.8 (26-Sep-2016)
2.7.7 (27-Aug-2016)
2.7.6 (23-Jul-2016)
2.7.5 (11-Jun-2016)
2.7.4 (29-Apr-2016)
2.7.3 (16-Mar-2016)

No changes since 2.7.2

2.7.2 (27-Feb-2016)

- Upgrade to Kotlin 1.0!

2.7.1 (04-Feb-2016)

- Upgrade to Kotlin 1.0.0-rc-1036, the first Release Candidate.

2.7.0 (10-Jan-2016)

No changes since 2.6.

2.6.6 (05-Apr-2016)

No changes since 2.6.5-2

2.6.5-2 (21-Feb-2016)

- Upgrade to Kotlin 1.0!

2.6.5 (20-Jan-2015)

- Upgrade to Kotlin 1.0.0-beta-4583

2.6.4 (07-Dec-2015)

- Upgrade to Kotlin 1.0.0-beta-2423 (Beta 2)
- Minor fixes to generic type handling.

2.6.3 (12-Oct-2015)

- Kotlin M14.

2.6.2 (16-Sep-2015)

- Upgrade to Kotlin M13.

2.6.1 (09-Aug-2015)
2.6.0 (19-Jul-2015)

No changes since 2.5.

2.5.5 (07-Dec-2015)

No changes since 2.5.4

2.5.4 (09-Jun-2015)

Update to Kotlin 0.11.91.4

2.5.3 (24-Apr-2015)
2.5.2 (29-Mar-2015)

No changes.

2.5.1-1 (19-Mar-2015)

Fixes to pass all unit tests, update to latest Kotlin version.

2.5.1 (06-Feb-2015)
2.5.0 (01-Jan-2015)

No changes since 2.4

2.4.4 (25-Nov-2014)

No changes since 2.4.3

2.4.3 (15-Oct-2014)

The First release.
