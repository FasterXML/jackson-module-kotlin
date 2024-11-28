Project: jackson-module-kotlin

Active Maintainers:

* Dmitry Spikhalskiy (@Spikhalskiy) (since 2.14)
* Drew Stephens (@dinomite)
* Vyacheslav Artemyev (@viartemev)
* WrongWrong (@k163377) (since 2.15)

Co-maintainers:

* Jayson Minard (@apatrida) -- original Module Author, helps from time to time
* Tatu Saloranta (@cowtowncoder) -- at-large Jackson Author, helps from time to time

------------------------------------------------------------------------
=== Releases ===
------------------------------------------------------------------------

2.18.2 (27-Nov-2024)
2.18.1 (28-Oct-2024)

No changes since 2.18.0

2.18.0 (26-Sep-2024)

#818: The implementation of the search process for the `JsonCreator` (often the primary constructor)
  used by default for deserialization has been changed to `AnnotationIntrospector#findDefaultCreator`.
  This has improved first-time processing performance and memory usage.
  It also solves the problem of `findCreatorAnnotation` results by `AnnotationIntrospector` registered by the user
  being ignored depending on the order in which modules are registered.
#817: The convertValue extension function now accepts null
#803: Kotlin has been upgraded to 1.8.10.
  The reason 1.8.22 is not used is to avoid KT-65156.
#782: Content marked as deprecated has been reorganized.
  Several constructors and accessors to properties of KotlinModule.Builder that were marked as DeprecationLevel.ERROR have been removed.
  Also, the content marked as DeprecationLevel.WARNING is now DeprecationLevel.ERROR.
#542: Remove meaningless checks and properties in KNAI.

2.17.3 (01-Nov-2024)

No changes since 2.17.2

2.17.2 (05-Jul-2024)

#799: Fixed problem with code compiled with 2.17.x losing backward compatibility.

2.17.1 (04-May-2024)

#776: Delete Duration conversion that was no longer needed.
#779: Errors no longer occur when processing Record types defined in Java.

2.17.0 (12-Mar-2024)

#768: Added value class deserialization support.
#760: Caching is now applied to the entire parameter parsing process on Kotlin.
#758: Deprecated SingletonSupport and related properties to be consistent with KotlinFeature.SingletonSupport.
#755: Changes in constructor invocation and argument management.
 This change degrades performance in cases where the constructor is called without default arguments, but improves performance in other cases.
#751: The KotlinModule#useKotlinPropertyNameForGetter property was deprecated because it differed from the name of the KotlinFeature.
 Please use KotlinModule#kotlinPropertyNameAsImplicitName from now on.
#747: Improved performance related to KotlinModule initialization and setupModule.
 With this change, the KotlinModule initialization error when using Kotlin 1.4 or lower has been eliminated.
#746: The KotlinModule#serialVersionUID is set to private.
#745: Modified isKotlinClass determination method.
#744: Functions that were already marked as deprecated,
 such as the primary constructor in KotlinModule and some functions in Builder,
 are scheduled for removal in 2.18 and their DeprecationLevel has been raised to Error.
 Hidden constructors that were left in for compatibility are also marked for removal.
 This PR also adds a hidden no-argument constructor to facilitate initialization from reflection.
 See the PR for details.
#743: The handling of deserialization using vararg arguments has been improved to allow deserialization even when the input to the vararg argument is undefined.
 In addition, vararg arguments are now reported as non-required.
#742: Minor performance improvements to NullToEmptyCollection/Map.
#741: Changed to allow KotlinFeature to be set in the function that registers a KotlinModule.
 The `jacksonObjectMapper {}` and `registerKotlinModule {}` lambdas allow configuration for KotlinModule.
#740: Reduce conversion cache from Executable to KFunction.
 This will reduce memory usage efficiency and total memory consumption, but may result in a minor performance degradation in use cases where a large number of factory functions are used as JsonCreator.
#738: JacksonInject is now preferred over the default argument(fixes #722).
#732: SequenceSerializer removed.
#727: Fixed overriding findCreatorAnnotation instead of hasCreatorAnnotation.

2.16.2 (09-Mar-2024)

No changes since 2.16.1

2.16.1 (24-Dec-2023)

#733: Fix problem with Serializable objects not implementing readResolve.

2.16.0 (15-Nov-2023)

#707: If JsonSetter(nulls = Nulls.SKIP) is specified, the default argument is now used when null.
#700: Reduce the load on the search process for serializers.
#689: Added UseJavaDurationConversion feature.
 By enabling this feature and adding the Java Time module, Kotlin Duration can be handled in the same way as Java Duration.
#687: Optimize and Refactor KotlinValueInstantiator.createFromObjectWith.
 This improves deserialization throughput about 1.3 ~ 1.5 times faster.
 https://github.com/FasterXML/jackson-module-kotlin/pull/687#issuecomment-1637365799
#686: Added KotlinPropertyNameAsImplicitName feature to use Kotlin property names as implicit names for getters.
 Enabling this feature eliminates some of the problems summarized in #630,
 but also causes some behavioral changes and performance degradation.
 A minor correction has been made to this option in #710.
#685: Streamline default value management for KotlinFeatures.
 This improves the initialization cost of kotlin-module a little.
#684: Kotlin 1.5 has been deprecated and the minimum supported Kotlin version will be updated to 1.6.

2.15.4 (15-Feb-2024)
2.15.3 (12-Oct-2023)

No changes since 2.15.2

2.15.2 (30-May-2023)

#675: Modified to use Converter in Sequence serialization
 This change allows serialization-related annotations, such as `JsonSerialize(contentUsing = ...)`, to work on `Sequence`.
 Also fixes #674.

2.15.1 (16-May-2023)

No changes since 2.15.0

2.15.0 (23-Apr-2023)

jackson-module-kotlin changes the serialization result of getter-like functions starting with 'is'.
For example, a function defined as `fun isValid(): Boolean`, which was previously output with the name `valid`, is now output with the name `isValid`.
See #670 for details.

#396: (regression) no default no-arguments constructor found
 (fix via [jackson-dataformat-xml#547])
#554: Add extension function for addMixin
 (contributing by Sylvain-maillard)
#580: Lazy load UNIT_TYPE
 (contributed by Ilya R)
#627: Merge creator cache for Constructor and Method(related to #584)
 (contributed by wrongwrong)
#628: Remove unnecessary cache(related to #584)
 (contributed by wrongwrong)
#629: Changed to not cache valueParameters(related to #584)
 (contributed by wrongwrong)
#631: Fix minor bugs in SimpleModule.addSerializer/addDeserializer(fixes #558)
 (contributed by wrongwrong)
#634: Fix ReflectionCache to be serializable(fixes #295)
 (contributed by wrongwrong)
#641: Fixed is-getter names to match parameters and fields(fixes #340)
 (contributed by wrongwrong)
#646: Drop Kotlin 1.4 support from Kotlin module 2.15.
    If Kotlin 1.4 or lower is detected, a JsonMappingException will be thrown when initializing a KotlinModule.
 (contributed by wrongwrong, cowtowncoder, pjfanning)
#647: Added deprecation to MissingKotlinParameterException(related to #617)
 (contributed by wrongwrong)
#652: Deletion of unused methods(fixes #508)
 (contributed by wrongwrong)
#654: Change MKPE.parameter property to transient(fixes #572)
 (contributed by wrongwrong)
#659: Improve serialization support for value class.
    Fixes #618 and partially fixes #625.
    Also fixed is serialization when a getter-like function returns a value class, and behavior when a getter is annotated with a `JsonSerialize` annotation.
 (contributed by wrongwrong)
#665: Modified to not load the entire Sequence into memory during serialization(fixes #368).
 (contributed by wrongwrong)
#666: Fixed problem with value class where JsonValue flag was ignored.
 (contributed by wrongwrong)
#667: Support JsonKey in value class.
 (contributed by wrongwrong)

It is also confirmed that the issue submitted below is no longer reproduced,
although it is unclear when it was explicitly fixed.

* #237
* #301

2.14.3 (05-May-2023)
2.14.2 (28-Jan-2023)
2.14.1 (21-Nov-2022)

No changes since 2.14.0

2.14.0 (05-Nov-2022)

#582: Ignore open-ended ranges in `KotlinMixins.kt` (to help with Kotlin 1.7.20+)
 (contributed by Richard K)
#586: 2.14.0-rc2 does not work with Kotlin 1.4 (requires 1.6)
 (@Spikhalskiy)

2.13.4 (03-Sep-2022)

#556: Broken Kotlin 1.4 support in 2.13.2
 (contributed by Dmitry S)

2.13.3 (14-May-2022)

No changes since 2.13.2

2.13.2 (17-Mar-2022) -- delayed due to compatibility issues, compared
  to other components that were released on (06-Mar-2022)

No changes since 2.13.1?

2.13.1 (19-Dec-2021)

* #456: Refactor KNAI.findImplicitPropertyName()
 (contributed by wrongwrong)
* #449: Refactor AnnotatedMethod.hasRequiredMarker()
 (contributed by wrongwrong)
* #521: Fixed lookup of instantiators
 (contributed by wrongwrong)

Dmitri Domanine (novtor@github)
* Contributed fix for #490: Missing value of type JsonNode? is deserialized as NullNode instead of null

2.13.0 (30-Sep-2021)

#438: Fixed mapping failure when `private` `companion object` is named
 (reported, fix contributed by k163377@github)
#447: Fix edge case when dealing with sealed classes
#468: Improved support for value classes
#477: Improved documentation for KotlinFeature
#489: Extension functions for JsonNode, ArrayNode and ObjectNode
#490: Fix deserialization of missing value (was `NullNode`, now literal `null`)
#494: Improved documentation for ProGuard users
#496: Fix type erasure in treeToValue() extension function

2.12.7 (26-May-2022)
2.12.6 (15-Dec-2021)
2.12.5 (27-Aug-2021)
2.12.4 (06-Jul-2021)
2.12.3 (12-Apr-2021)

No recorded changes since 2.12.2

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
