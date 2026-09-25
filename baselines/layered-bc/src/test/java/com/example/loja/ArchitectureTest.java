package com.example.loja;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    private static com.tngtech.archunit.core.domain.JavaClasses classes;

    @BeforeAll
    static void importar() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.example.loja");
    }

    @Test
    void controllerDeCatalogoNaoAcessaRepositoryOuClient() {
        noClasses().that().resideInAPackage("..catalogo.controller..")
                .should().dependOnClassesThat().resideInAnyPackage("..catalogo.repository..")
                .check(classes);
    }

    @Test
    void controllerDePedidosNaoAcessaRepositoryOuClient() {
        noClasses().that().resideInAPackage("..pedidos.controller..")
                .should().dependOnClassesThat().resideInAnyPackage("..pedidos.repository..", "..pedidos.client..")
                .check(classes);
    }

    @Test
    void repositoryDeCatalogoSoAcessadoPeloServiceDoMesmoContexto() {
        classes().that().resideInAPackage("..catalogo.repository..")
                .should().onlyBeAccessed().byClassesThat()
                .resideInAnyPackage("..catalogo.repository..", "..catalogo.service..")
                .check(classes);
    }

    @Test
    void repositoryEClientDePedidosSoAcessadosPeloServiceDoMesmoContexto() {
        classes().that().resideInAnyPackage("..pedidos.repository..", "..pedidos.client..")
                .should().onlyBeAccessed().byClassesThat()
                .resideInAnyPackage("..pedidos.repository..", "..pedidos.client..", "..pedidos.service..")
                .check(classes);
    }

    @Test
    void pedidosNaoAcessaControllerRepositoryOuModelDeCatalogo() {
        noClasses().that().resideInAPackage("..pedidos..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..catalogo.controller..", "..catalogo.repository..", "..catalogo.model..")
                .check(classes);
    }

    @Test
    void catalogoNaoAcessaControllerRepositoryModelOuClientDePedidos() {
        noClasses().that().resideInAPackage("..catalogo..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..pedidos.controller..", "..pedidos.repository..", "..pedidos.model..", "..pedidos.client..")
                .check(classes);
    }

    @Test
    void sharedNaoDependeDeCatalogoNemDePedidos() {
        noClasses().that().resideInAPackage("..shared..")
                .should().dependOnClassesThat().resideInAnyPackage("..catalogo..", "..pedidos..")
                .check(classes);
    }

    @Test
    void apenasLojaApplicationResideNoPacoteRaiz() {
        classes().that().resideInAPackage("com.example.loja")
                .should().haveSimpleName("LojaApplication")
                .check(classes);
    }
}
