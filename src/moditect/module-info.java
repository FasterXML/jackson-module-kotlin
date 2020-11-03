// Manually created 02-Nov-2020 for
//   https://github.com/FasterXML/jackson-module-kotlin/issues/385
module com.fasterxml.jackson.kotlin {
    requires java.base;
    requires java.desktop;
    requires kotlin.stdlib;

    requires com.fasterxml.jackson.annotations;
    requires com.fasterxml.jackson.databind;

    provides com.fasterxml.jackson.databind.Module with
        com.fasterxml.jackson.module.kotlin.KotlinModule;
}
