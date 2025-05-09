Here are people who have contributed to development Jackson JSON processor
Kotlin module, version 2.x
(version numbers in brackets indicate release in which the problem was fixed)

Authors:

  Jayson Minard/@apatrida, jayson.minard@gmail.com: original author

  Tatu Saloranta/@cowtowncoder (tatu.saloranta@iki.fi) : co-author

  Dmitry Spikhalskiy (@Spikhalskiy) co-author (since 2.14)
  Drew Stephens/@dinomite (drew@dinomite.net) co-author (since 2.11)
  Vyacheslav Artemyev (@viartemev) co-author (since 2.11)
  WrongWrong (@k163377) co-author (since 2.15)

Contributors:

# 2.20.0 (not yet released)

WrongWrong (@k163377)
* #969: Cleanup of deprecated contents
* #967: Update settings for 2.20

# 2.19.1 (not yet released)

# 2.19.0 (24-Apr-2025)

WrongWrong (@k163377)
* #959: Add extensions for configOverride
* #954: Replace BooleanTriState with OptBoolean
* #944: Fixed to use common util for Member accessibility override
* #937: Added type match check to read functions

Tatu Saloranta (@cowtowncoder)
* #889: Upgrade kotlin dep to 1.9.25 (from 1.9.24)

WrongWrong (@k163377)
* #930: Add tests for #917
* #929: Bug fixes to hasRequiredMarker and added isRequired considerations
* #914: Add test case to serialize Nothing? (for #314)
* #910: Add default KeyDeserializer for value class
* #885: Performance improvement of strictNullChecks
* #884: Changed the base class of MissingKotlinParameterException to InvalidNullException
* #878: Fix for #876
* #868: Added test case for FAIL_ON_NULL_FOR_PRIMITIVES
* #866: Upgrade to JUnit5
* #861: Update Kotlin to 1.9.24
* #858: Refactor findDefaultCreator
* #839: Remove useKotlinPropertyNameForGetter and unify with kotlinPropertyNameAsImplicitName
* #835: Remove old SingletonSupport class and unified with KotlinFeature.SingletonSupport

# 2.18.4 (not yet released)

WrongWrong (@k163377)
* #923: Fixed hasRequiredMarker to only process content defined in Kotlin
* #920: Minor refactors that do not affect behavior

# 2.18.3 (28-Feb-2025)

WrongWrong (@k163377)
* #908: Additional fixes related to #904.
* #904: Fixed an error when serializing a `value class` that wraps a `Map`
* #900: Fixed an issue where some tests were not running

# 2.18.0 (26-Sep-2024)

WrongWrong (@k163377)
* #883: Raise the deprecation level to error for the MissingKotlinParameterException secondary constructor
* #869: Replaced Enum.values with Enum.entries
* #818: Optimize the search process for creators
* #817: Fixed nullability of convertValue function argument
* #782: Organize deprecated contents
* #542: Remove meaningless checks and properties in KNAI

# 2.17.2 (05-Jul-2024)

WrongWrong (@k163377)
* #799: Fixed problem with code compiled with 2.17.x losing backward compatibility.

# 2.17.1 (04-May-2024)

WrongWrong (@k163377)
* #776: Delete Duration conversion that was no longer needed
* #779: Fixed to not process constructors of Java classes

# 2.17.0

WrongWrong (@k163377)
* #768: Added value class deserialization support.
* #763: Minor refactoring to support value class in deserialization.
* #760: Improved processing related to parameter parsing on Kotlin.
* #759: Organize internal commons.
* #758: Deprecated SingletonSupport.
* #755: Changes in constructor invocation and argument management.
* #752: Fix KDoc for KotlinModule.
* #751: Marked useKotlinPropertyNameForGetter as deprecated.
* #747: Improved performance related to KotlinModule initialization and setupModule.
* #746: The KotlinModule#serialVersionUID is set to private.
* #745: Modified isKotlinClass determination method.
* #744: API deprecation update for KotlinModule.
* #743: Fix handling of vararg deserialization.
* #742: Minor performance improvements to NullToEmptyCollection/Map.
* #741: Changed to allow KotlinFeature to be set in the function that registers a KotlinModule.
* #740: Reduce conversion cache from Executable to KFunction.
* #738: Fix JacksonInject priority.
* #732: SequenceSerializer removed.
* #727: Fixed overriding findCreatorAnnotation instead of hasCreatorAnnotation

# 2.16.1

WrongWrong (@k163377)
* #733: Fix problem with Serializable objects not implementing readResolve

# 2.16.0

kkurczewski
* #689: Add KotlinDuration support

WrongWrong (@k163377)
* #710: Fixed KotlinPropertyNameAsImplicitName not working in edge case
* #709: Add failing test for #242
* #707: Changed to use default argument on null if JsonSetter(nulls = Nulls.SKIP) is specified.
* #700: Reduce the load on the search process for serializers
* #687: Optimize and Refactor KotlinValueInstantiator.createFromObjectWith
* #686: Add KotlinPropertyNameAsImplicitName option
* #685: Streamline default value management for KotlinFeatures
* #684: Update Kotlin Version to 1.6

# 2.15.2

WrongWrong (@k163377)
* #675: Modified to use Converter in Sequence serialization

# 2.15.0

Ilya Ryzhenkov (@orangy)
* #580: Lazy load UNIT_TYPE

WrongWrong (@k163377)
* #627: Merge creator cache for Constructor and Method
* #628: Remove unnecessary cache
* #629: Changed to not cache valueParameters
* #631: Fix minor bugs in SimpleModule.addSerializer/addDeserializer
* #634: Fix ReflectionCache to be serializable
* #640: Fixed problem with test failure in windows.
* #641: Fixed is-getter names to match parameters and fields.
* #646: In 2.15, fixed to not be able to use Kotlin 1.4 or lower.
* #647: Added deprecation to MissingKotlinParameterException.
* #652: Deletion of unused methods.
* #654: Change MKPE.parameter property to transient.
* #659: Improve serialization support for value class.
* #665: Modified to not load the entire Sequence into memory during serialization.
* #666: Fixed problem with value class where JsonValue flag was ignored.
* #667: Support JsonKey in value class.

Sylvain-maillard (@Sylvain-maillard)
* #554: Add extension function for addMixin.

# 2.14.0

Richard Kwasnicki (Richie94@github)
* #582: Ignore open-ended ranges in `KotlinMixins.kt` (to help with Kotlin 1.7.20+)

# 2.13.4

Dmitry Spikhalskiy (Spikhalskiy@github)
* #556: Broken Kotlin 1.4 support

# 2.13.1

Stefan Schmid (schmist@github)
* #519: Contributed test for #518 (null should deserialize to _the_ Unit instance)

wrongwrong (k163377@github)
* #456: Refactor KNAI.findImplicitPropertyName()
* #449: Refactor AnnotatedMethod.hasRequiredMarker()
* #521: Fixed lookup of instantiators
* #527: Improvements to serialization of `value class`.

Dmitri Domanine (novtor@github)
* Contributed fix for #490: Missing value of type JsonNode? is deserialized as NullNode instead of null

# 2.13.0

Fedor Bobin (Fuud@github)
* #496, #45: Fix treeToValue extension function should not have type erasure

Mikhael Sokolov (sokomishalov@github)
* #489: JsonNode, ArrayNode and ObjectNode extension functions

Max Wiechmann (MaxMello@github)
* #494: ProGuard ProTips in the README

Róbert Papp (TWiStErRob@github)
* #477: KotlinFeature documentation & deprecation replacements

wrongwrong (k163377@github)
* #468: Improved support for value classes

wrongwrong (k163377@github)
* #460: Test for GitHub #451 (`-` in property name handling)

wrongwrong (k163377@github)
* #447: Fix edge case when dealing with sealed classes

wrongwrong (k163377@github)
* Contributed #438: Fixed mapping failure when `private` `companion object` is named

# 2.12.5

Marshall Pierce (marshallpierce@github)
* #474: Reported disrespect for @JsonProperty on parent class

Christopher Mason (masoncj@github)
* #194: Contributed test case for @JsonIdentityInfo usage

Martin Häusler (MartinHaeusler@github)
* Reported #194: @JsonIdentityInfo bug

# 2.12.2

Eric Fenderbosch (efenderbosch@github)
* Fixed #182: Serialize unsigned numbers

Elisha Peterson (triathematician@github)
* Reported #409: `module-info.java` missing "exports"

# 2.12.1

Wolfgang Jung (elektro-wolle@github)
* Fixed inline class serialization

# 2.12.0

John Flynn (Neuman968@github)
* Contributed extension methods for SimpleModule to add serializer and deserializer
  extension functions for KClass #322

Mateusz Stefek (MateuszStefek@github)
* Reported #321: Make MissingKotlinParameterException a descendant of MismatchedInputException

Hideaki Tanabe (tanabe@github)
* Brought README.md into the modern world of Gradle (compile -> implementation)

Hidde Wieringa (hiddewie@github)
* Contributed test case for issue 308
* Contributed Kotlin DSL constructor

David Riggleman (DavidRigglemanININ@github)
* Wrote strict null checking for collection values

# 2.11.0

Drew Stephens (dinomite@github)
* Contributed fix for #281: KotlinObjectSingletonDeserializer fails to deserialize
  previously serialized JSON as it doesn't delegate deserializeWithType

# 2.10.2

Patrick Strawderman (kilink@github)
* Reported #279: 2.10 introduces another binary compatibility issue in `KotlinModule`
  constructor

Vladimir Petrakovich (frost13it@github)
* Contributed fix for #279: 2.10 introduces another binary compatibility issue in
  `KotlinModule` constructor

# 2.10.1

Stéphane B (StephaneBg@github)
* Submitted fix for #176: Version 2.9.7 breaks compatibility with Android minSdk < 24

Alain Lehmann (ciderale@github)
* Contributed fix for #225: Don't instantiate new instances of Kotlin singleton objects

Andrey Lipatov (LipatovAndrey@github)
* Contributed fix for #80: Boolean property name starting with 'is' not serialized /
  deserialized properly

Konstantin Volivach (kostya05983@github)
* Contributed fix for #254: Add serializers for Sequences

Laimonas Turauskas (Laimiux@github)
* Contributed fix for #180: handle nullable method parameters correctly (for creator methods)

