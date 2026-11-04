package br.com.fiap.rei_dos_piratas;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.base.DescribedPredicate.describe;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Regras de camada da Clean Architecture (infrastructure -> interfaces -> application -> domain).
 * Estritas: qualquer violação faz o teste falhar (o congelamento usado durante a refatoração foi removido).
 * Dívida conhecida fora destas regras: anotações do Jackson em domain/entity/Usuario.
 */
@AnalyzeClasses(packages = "br.com.fiap.rei_dos_piratas", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaTest {

    @ArchTest
    static final ArchRule dominioEAplicacaoNaoDependemDasCamadasExternas =
            noClasses()
                    .that().resideInAnyPackage("..domain..", "..application..")
                    .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..interfaces..")
                    .because("a regra de dependência é infrastructure -> interfaces -> application -> domain");

    @ArchTest
    static final ArchRule dominioNaoDependeDaAplicacao =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("..application..");

    @ArchTest
    static final ArchRule interfacesNaoDependeDaInfraestrutura =
            noClasses()
                    .that().resideInAPackage("..interfaces..")
                    .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                    .because("controllers e DTOs de aplicação não conhecem Feign, JPA nem segurança");

    @ArchTest
    static final ArchRule dominioNaoDependeDoSpring =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                    .because("o domínio é Java puro (Lombok e Bean Validation são tolerados)");

    @ArchTest
    static final ArchRule aplicacaoSoUsaSpringParaTransacao =
            noClasses()
                    .that().resideInAPackage("..application..")
                    .should().dependOnClassesThat(describe("do Spring fora de org.springframework.transaction",
                            (JavaClass classe) -> classe.getPackageName().startsWith("org.springframework")
                                    && !classe.getPackageName().startsWith("org.springframework.transaction")))
                    .because("configuração (@Value, beans) fica em infrastructure/config");

    @ArchTest
    static final ArchRule dominioEAplicacaoNaoUsamFeign =
            noClasses()
                    .that().resideInAnyPackage("..domain..", "..application..")
                    .should().dependOnClassesThat().resideInAPackage("feign..")
                    .because("integrações externas entram por portas em domain/gateway");

    @ArchTest
    static final ArchRule beansSaoMontadosNaInfraestrutura =
            noClasses()
                    .that().resideInAnyPackage("..domain..", "..application..", "..interfaces..")
                    .should().beAnnotatedWith(Service.class)
                    .orShould().beAnnotatedWith(Component.class)
                    .orShould().beAnnotatedWith(Configuration.class)
                    .because("os beans são montados por @Bean em infrastructure/config");
}
