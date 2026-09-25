package com.example.loja.arch;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;

import org.junit.jupiter.api.Test;

class ArquiteturaTest {

    private final com.tngtech.archunit.core.domain.JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.example.loja");

    @Test
    void entityNaoDependeDeOutrosAneisNemDeFramework() {
        ArchRule rule = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
                .that().resideInAPackage("com.example.loja.entity..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("com.example.loja.usecase..", "com.example.loja.adapter..",
                        "com.example.loja.infrastructure..")
                .orShould().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", "jakarta.persistence..");
        rule.check(classes);
    }

    @Test
    void usecaseDependeApenasDeEntity() {
        ArchRule rule = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
                .that().resideInAPackage("com.example.loja.usecase..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("com.example.loja.adapter..", "com.example.loja.infrastructure..")
                .orShould().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", "jakarta.persistence..");
        rule.check(classes);
    }

    @Test
    void adapterNaoDependeDeInfrastructure() {
        ArchRule rule = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
                .that().resideInAPackage("com.example.loja.adapter..")
                .should().dependOnClassesThat()
                .resideInAPackage("com.example.loja.infrastructure..");
        rule.check(classes);
    }

    @Test
    void regraDeDependenciaEntreAneis() {
        ArchRule rule = Architectures.layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                .layer("Entity").definedBy("com.example.loja.entity..")
                .layer("UseCase").definedBy("com.example.loja.usecase..")
                .layer("Adapter").definedBy("com.example.loja.adapter..")
                .layer("Infrastructure").definedBy("com.example.loja.infrastructure..")
                .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
                .whereLayer("Adapter").mayOnlyBeAccessedByLayers("Infrastructure")
                .whereLayer("UseCase").mayOnlyBeAccessedByLayers("Adapter", "Infrastructure")
                .whereLayer("Entity").mayOnlyBeAccessedByLayers("UseCase", "Adapter", "Infrastructure");
        rule.check(classes);
    }
}
