package br.com.fiap.rei_dos_piratas;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.freeze.FreezingArchRule.freeze;

/**
 * Regra de dependência da Clean Architecture: domain e application não conhecem as camadas externas.
 * As violações existentes estão congeladas em src/test/resources/archunit_store (dívida da refatoração);
 * qualquer violação nova faz o teste falhar.
 */
@AnalyzeClasses(packages = "br.com.fiap.rei_dos_piratas", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaTest {

    @ArchTest
    static final ArchRule dominioEAplicacaoNaoDependemDasCamadasExternas = freeze(
            noClasses()
                    .that().resideInAnyPackage("..domain..", "..application..")
                    .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..interfaces..")
                    .because("a regra de dependência é infrastructure -> interfaces -> application -> domain"));

    @ArchTest
    static final ArchRule dominioNaoDependeDaAplicacao =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("..application..");
}
