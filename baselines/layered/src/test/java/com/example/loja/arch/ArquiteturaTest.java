package com.example.loja.arch;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArquiteturaTest {

    private static final String BASE_PACKAGE = "com.example.loja";

    private final com.tngtech.archunit.core.domain.JavaClasses classesImportadas =
            new ClassFileImporter().importPackages(BASE_PACKAGE);

    @Test
    void nenhumaCamadaDependeDoController() {
        ArchRule regra = noClasses()
                .that().resideOutsideOfPackage(BASE_PACKAGE + ".controller..")
                .should().dependOnClassesThat().resideInAPackage(BASE_PACKAGE + ".controller..");
        regra.check(classesImportadas);
    }

    @Test
    void controllerNaoAcessaRepositoryNemClient() {
        ArchRule regra = noClasses()
                .that().resideInAPackage(BASE_PACKAGE + ".controller..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        BASE_PACKAGE + ".repository..", BASE_PACKAGE + ".client..");
        regra.check(classesImportadas);
    }

    @Test
    void repositorySoAcessadoPorService() {
        ArchRule regra = classes()
                .that().resideInAPackage(BASE_PACKAGE + ".repository..")
                .should().onlyBeAccessed().byAnyPackage(
                        BASE_PACKAGE + ".repository..", BASE_PACKAGE + ".service..");
        regra.check(classesImportadas);
    }

    @Test
    void clientSoAcessadoPorService() {
        ArchRule regra = classes()
                .that().resideInAPackage(BASE_PACKAGE + ".client..")
                .should().onlyBeAccessed().byAnyPackage(
                        BASE_PACKAGE + ".client..", BASE_PACKAGE + ".service..");
        regra.check(classesImportadas);
    }
}
