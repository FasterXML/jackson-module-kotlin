// Manually created 02-Nov-2020 for
//   https://github.com/FasterXML/jackson-module-kotlin/issues/385
module com.fasterxml.jackson.kotlin {
    requires java.desktop;

    requires kotlin.reflect;
    requires kotlin.stdlib;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;

    exports tools.jackson.module.kotlin;

    provides com.fasterxml.jackson.databind.Module with
        tools.jackson.module.kotlin.KotlinModule;
}
