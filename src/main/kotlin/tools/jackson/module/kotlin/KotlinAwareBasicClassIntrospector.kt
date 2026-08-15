package tools.jackson.module.kotlin

import tools.jackson.databind.JavaType
import tools.jackson.databind.cfg.MapperConfig
import tools.jackson.databind.introspect.AccessorNamingStrategy
import tools.jackson.databind.introspect.AnnotatedClass
import tools.jackson.databind.introspect.BasicClassIntrospector
import tools.jackson.databind.introspect.POJOPropertiesCollector

internal class KotlinAwareBasicClassIntrospector : BasicClassIntrospector {
    constructor() : super()
    constructor(config: MapperConfig<*>) : super(config)

    override fun forMapper(): KotlinAwareBasicClassIntrospector =
        if (_config == null) this else KotlinAwareBasicClassIntrospector()

    override fun forOperation(config: MapperConfig<*>): KotlinAwareBasicClassIntrospector =
        KotlinAwareBasicClassIntrospector(config)

    override fun constructPropertyCollector(
        type: JavaType,
        classDef: AnnotatedClass,
        forSerialization: Boolean,
        accNaming: AccessorNamingStrategy,
    ): POJOPropertiesCollector {
        if (type.rawClass.isKotlinClass()) {
            return KotlinAwarePOJOPropertiesCollector(_config, forSerialization, type, classDef, accNaming)
        }
        return super.constructPropertyCollector(type, classDef, forSerialization, accNaming)
    }
}
