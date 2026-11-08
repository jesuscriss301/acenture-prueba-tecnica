package com.accenture.franchiseapi;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Reglas de arquitectura hexagonal verificadas automáticamente.
 * Si alguien rompe una dependencia entre capas o entre módulos, esta prueba falla.
 */
@AnalyzeClasses(packages = "com.accenture.franchiseapi", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainIsIndependent = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..application..", "..infrastructure..",
                    "org.springframework..", "software.amazon..", "reactor..")
            .because("el dominio no depende de casos de uso, infraestructura ni frameworks");

    @ArchTest
    static final ArchRule applicationDoesNotDependOnInfrastructure = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..infrastructure..", "org.springframework..", "software.amazon..")
            .because("los casos de uso solo conocen el dominio y sus puertos");

    @ArchTest
    static final ArchRule adaptersImplementOutputPorts = classes()
            .that().haveSimpleNameEndingWith("DynamoDbAdapter")
            .should().implement(resideInAPackage("..application.port.out.."))
            .because("cada adaptador de persistencia implementa un puerto de salida");

    @ArchTest
    static final ArchRule servicesImplementInputPorts = classes()
            .that().resideInAPackage("..application.service..")
            .should().implement(resideInAPackage("..application.port.in.."))
            .because("cada servicio implementa un caso de uso (puerto de entrada)");

    @ArchTest
    static final ArchRule franchiseInternalsAreEncapsulated = featureInternalsAreEncapsulated("franchise");

    @ArchTest
    static final ArchRule branchInternalsAreEncapsulated = featureInternalsAreEncapsulated("branch");

    @ArchTest
    static final ArchRule productInternalsAreEncapsulated = featureInternalsAreEncapsulated("product");

    /**
     * Un módulo solo puede usar otro a través de su dominio o de sus puertos de salida;
     * nunca su infraestructura, sus servicios ni sus casos de uso.
     */
    private static ArchRule featureInternalsAreEncapsulated(String feature) {
        String base = "com.accenture.franchiseapi." + feature;
        return noClasses()
                .that().resideOutsideOfPackage(base + "..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        base + ".infrastructure..",
                        base + ".application.service..",
                        base + ".application.port.in..")
                .because("los módulos se comunican solo por puertos");
    }
}
