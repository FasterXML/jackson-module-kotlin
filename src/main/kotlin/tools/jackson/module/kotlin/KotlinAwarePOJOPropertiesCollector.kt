package tools.jackson.module.kotlin

import tools.jackson.databind.JavaType
import tools.jackson.databind.cfg.MapperConfig
import tools.jackson.databind.introspect.AccessorNamingStrategy
import tools.jackson.databind.introspect.AnnotatedClass
import tools.jackson.databind.introspect.POJOPropertiesCollector
import tools.jackson.databind.introspect.POJOPropertyBuilder

internal class KotlinAwarePOJOPropertiesCollector(
    config: MapperConfig<*>,
    forSerialization: Boolean,
    type: JavaType,
    classDef: AnnotatedClass,
    accessorNaming: AccessorNamingStrategy,
) : POJOPropertiesCollector(config, forSerialization, type, classDef, accessorNaming) {

    override fun _addMethods(props: MutableMap<String, POJOPropertyBuilder>) {
        for (method in KotlinMethodCollectorFix.correctedMemberMethods(_classDef)) {
            when (method.parameterCount) {
                0 -> _addGetterMethod(props, method)
                1 -> _addSetterMethod(props, method)
                2 -> if (java.lang.Boolean.TRUE == _annotationIntrospector.hasAnySetter(_config, method)) {
                    if (_anySetters == null) {
                        _anySetters = java.util.LinkedList()
                    }
                    _anySetters.add(method)
                }
            }
        }
    }
}
