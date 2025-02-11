// Kotlin module-info for (unit) Tests
module tools.jackson.module.kotlin
{
    // Since we are not split from Main artifact, will not
    // need to depend on Main artifact -- but need its dependencies

    requires kotlin.reflect;
    requires kotlin.stdlib;
    requires org.jetbrains.annotations;

    requires com.fasterxml.jackson.annotation;
    requires tools.jackson.core;
    requires tools.jackson.databind;

    // Additional test lib/framework dependencies
    requires kotlin.test;
    requires org.junit.jupiter.api;

    // Other test deps
    requires tools.jackson.dataformat.xml;

    // Further, need to open up test packages for JUnit et al

    exports tools.jackson.module.kotlin;
    opens tools.jackson.module.kotlin;
    exports tools.jackson.module.kotlin.kogeraIntegration;
    opens tools.jackson.module.kotlin.kogeraIntegration;
    exports tools.jackson.module.kotlin.kogeraIntegration.deser;
    opens tools.jackson.module.kotlin.kogeraIntegration.deser;
    exports tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass;
    opens tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass;

    exports tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.deserializer;
    opens tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.deserializer;

    exports tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.deserializer.byAnnotation;
    opens tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.deserializer.byAnnotation;

    exports tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.deserializer.byAnnotation.specifiedForProperty;
    opens tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.deserializer.byAnnotation.specifiedForProperty;

    exports tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.jsonCreator;
    opens tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.jsonCreator;

    exports tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.parameterSize.nonNullObject;
    opens tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.parameterSize.nonNullObject;

    exports tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.parameterSize.nullableObject;
    opens tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.parameterSize.nullableObject;

    exports tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.parameterSize.primitive;
    opens tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.parameterSize.primitive;

    exports tools.jackson.module.kotlin.test;
    opens tools.jackson.module.kotlin.test;

    exports tools.jackson.module.kotlin.test.github;
    opens tools.jackson.module.kotlin.test.github;

    exports tools.jackson.module.kotlin.test.github.failing;
    opens tools.jackson.module.kotlin.test.github.failing;

    exports tools.jackson.module.kotlin.test.github.parameterSize;
    opens tools.jackson.module.kotlin.test.github.parameterSize;
}
