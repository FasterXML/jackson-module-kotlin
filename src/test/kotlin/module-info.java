// Kotline module-info for (unit) Tests
module com.fasterxml.jackson.kotlin
{
    // Since we are not split from Main artifact, will not
    // need to depend on Main artifact -- but need its dependencies

    requires java.desktop;

    requires kotlin.reflect;
    requires kotlin.stdlib;
    requires org.jetbrains.annotations;

    requires com.fasterxml.jackson.annotation;
    requires tools.jackson.databind;

    // Additional test lib/framework dependencies
    requires junit; // JUnit 4

    // Other test deps

    // Further, need to open up test packages for JUnit et al

    opens tools.jackson.module.kotlin;
    opens tools.jackson.module.kotlin.kogeraIntegration;
    opens tools.jackson.module.kotlin.kogeraIntegration.deser;
    opens tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass;
    opens tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.deserializer;
    opens tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.deserializer.byAnnotation;
    opens tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.deserializer.byAnnotation.specifiedForProperty;
    opens tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.jsonCreator;
    opens tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.parameterSize.nonNullObject;
    opens tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.parameterSize.nullableObject;
    opens tools.jackson.module.kotlin.kogeraIntegration.deser.valueClass.parameterSize.primitive;
    opens tools.jackson.module.kotlin.test;
    opens tools.jackson.module.kotlin.test.github;
    opens tools.jackson.module.kotlin.test.github.failing;
    opens tools.jackson.module.kotlin.test.github.parameterSize;
}
