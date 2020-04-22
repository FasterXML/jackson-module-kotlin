Here are people who have contributed to development Jackson JSON processor
Kotlin module, version 2.x
(version numbers in brackets indicate release in which the problem was fixed)

Authors:

  Jayson Minard/@apatrida, jayson.minard@gmail.com: author

  Tatu Saloranta/@cowtowncoder (tatu.saloranta@iki.fi) : co-author

  Drew Stephens/@dinomite (drew@dinomite.net) co-author (since 2.11)

Contributors:

Stéphane B (StephaneBg@github)
* Submitted fix for #176: Version 2.9.7 breaks compatibility with Android minSdk < 24
 (2.10.1)
 
Alain Lehmann (ciderale@github)
* Contributed fix for #225: Don't instantiate new instances of Kotlin singleton objects
 (2.10.1)

Andrey Lipatov (LipatovAndrey@github)
* Contributed fix for #80: Boolean property name starting with 'is' not serialized /
  deserialized properly
 (2.10.1)

Konstantin Volivach (kostya05983@github)
* Contributed fix for #254: Add serializers for Sequences
 (2.10.1)

Laimonas Turauskas (Laimiux@github)
* Contributed fix for #180: handle nullable method parameters correctly (for creator methods)
 (2.10.1)

Patrick Strawderman (kilink@github)
* Reported #279: 2.10 introduces another binary compatibility issue in `KotlinModule`
  constructor
 (2.10.2)

Vladimir Petrakovich (frost13it@github)
* Contributed fix for #279: 2.10 introduces another binary compatibility issue in
  `KotlinModule` constructor
 (2.10.2)

Drew Stephens (dinomite@github)
* Contributed fix for #281: KotlinObjectSingletonDeserializer fails to deserialize
    previously serialized JSON as it doesn't delegate deserializeWithType
  (2.11.0)
