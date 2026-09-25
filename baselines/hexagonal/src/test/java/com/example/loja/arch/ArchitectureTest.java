package com.example.loja.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;

class ArchitectureTest {

    private static final String ROOT = "com.example.loja";
    private static final JavaClasses IMPORTED = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(ROOT);

    @Test
    void domainNaoDependeDeApplicationAdapterOuConfig() {
        ArchRuleDefinition.noClasses()
                .that().resideInAPackage(ROOT + ".domain..")
                .should().dependOnClassesThat(resideInAPackage(ROOT + ".application.."))
                .orShould().dependOnClassesThat(resideInAPackage(ROOT + ".adapter.."))
                .orShould().dependOnClassesThat(resideInAPackage(ROOT + ".config.."))
                .check(IMPORTED);
    }

    @Test
    void domainNaoDependeDeSpringOuJpa() {
        ArchRuleDefinition.noClasses()
                .that().resideInAPackage(ROOT + ".domain..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta.persistence..")
                .check(IMPORTED);
    }

    @Test
    void applicationNaoDependeDeAdapterOuConfig() {
        ArchRuleDefinition.noClasses()
                .that().resideInAPackage(ROOT + ".application..")
                .should().dependOnClassesThat(resideInAPackage(ROOT + ".adapter.."))
                .orShould().dependOnClassesThat(resideInAPackage(ROOT + ".config.."))
                .check(IMPORTED);
    }

    @Test
    void applicationNaoDependeDeSpringOuJpa() {
        ArchRuleDefinition.noClasses()
                .that().resideInAPackage(ROOT + ".application..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta.persistence..")
                .check(IMPORTED);
    }

    @Test
    void adapterInNaoDependeDeAdapterOut() {
        ArchRuleDefinition.noClasses()
                .that().resideInAPackage(ROOT + ".adapter.in..")
                .should().dependOnClassesThat(resideInAPackage(ROOT + ".adapter.out.."))
                .check(IMPORTED);
    }

    @Test
    void adapterOutNaoDependeDeAdapterIn() {
        ArchRuleDefinition.noClasses()
                .that().resideInAPackage(ROOT + ".adapter.out..")
                .should().dependOnClassesThat(resideInAPackage(ROOT + ".adapter.in.."))
                .check(IMPORTED);
    }
}
