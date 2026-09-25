package eval.archcheck;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.EvaluationResult;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Verificador de conformidade arquitetural usado pelo harness.
 * Uso: java -jar archcheck.jar &lt;layered|layered-bc|hexagonal|clean&gt; &lt;dir de classes compiladas&gt;
 * Imprime JSON com as violações por regra. Sai com 0 se não houver violações, 1 caso contrário.
 */
public final class ArchCheck {

    private static final String ROOT = "com.example.loja";
    private static final String[] FRAMEWORKS = {"org.springframework..", "jakarta.persistence.."};

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("uso: archcheck <layered|layered-bc|hexagonal|clean> <classesDir>");
            System.exit(2);
        }
        JavaClasses classes = new ClassFileImporter().importPath(Path.of(args[1]));
        Map<String, ArchRule> rules = switch (args[0]) {
            case "layered" -> layered();
            case "layered-bc" -> layeredBc();
            case "hexagonal" -> hexagonal();
            case "clean" -> clean();
            default -> throw new IllegalArgumentException("arquitetura desconhecida: " + args[0]);
        };

        List<String> out = new ArrayList<>();
        int total = 0;
        for (var e : rules.entrySet()) {
            EvaluationResult result = e.getValue().allowEmptyShould(true).evaluate(classes);
            List<String> details = result.getFailureReport().getDetails();
            total += details.size();
            String sample = details.stream().limit(5).map(ArchCheck::json).collect(Collectors.joining(","));
            out.add("{\"rule\":" + json(e.getKey()) + ",\"violations\":" + details.size() + ",\"sample\":[" + sample + "]}");
        }
        System.out.println("{\"arch\":" + json(args[0]) + ",\"classes\":" + classes.size()
                + ",\"totalViolations\":" + total + ",\"rules\":[" + String.join(",", out) + "]}");
        System.exit(total == 0 ? 0 : 1);
    }

    private static String p(String sub) {
        return ROOT + "." + sub + "..";
    }

    private static ArchRule allowedPackages(String... subs) {
        String[] pkgs = java.util.Arrays.stream(subs).map(ArchCheck::p).toArray(String[]::new);
        return classes().that().resideOutsideOfPackage(ROOT).and().doNotHaveSimpleName("package-info")
                .should().resideInAnyPackage(pkgs);
    }

    static Map<String, ArchRule> layered() {
        Map<String, ArchRule> r = new LinkedHashMap<>();
        r.put("pacotes-permitidos", allowedPackages("controller", "service", "repository", "model", "dto", "client", "exception", "config"));
        r.put("camadas", layeredArchitecture().consideringOnlyDependenciesInLayers()
                .layer("Controller").definedBy(p("controller"))
                .layer("Service").definedBy(p("service"))
                .layer("Repository").definedBy(p("repository"))
                .layer("Client").definedBy(p("client"))
                .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
                .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller")
                .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service")
                .whereLayer("Client").mayOnlyBeAccessedByLayers("Service"));
        return r;
    }

    static Map<String, ArchRule> layeredBc() {
        Map<String, ArchRule> r = new LinkedHashMap<>();
        List<String> allowed = new ArrayList<>(List.of("shared"));
        for (String ctx : List.of("catalogo", "pedidos")) {
            for (String layer : List.of("controller", "service", "repository", "model", "dto", "client", "exception")) {
                allowed.add(ctx + "." + layer);
            }
        }
        r.put("pacotes-permitidos", allowedPackages(allowed.toArray(String[]::new)));
        for (String ctx : List.of("catalogo", "pedidos")) {
            String other = ctx.equals("catalogo") ? "pedidos" : "catalogo";
            r.put(ctx + ": controller não acessa repository/client",
                    noClasses().that().resideInAPackage(p(ctx + ".controller"))
                            .should().dependOnClassesThat().resideInAnyPackage(p(ctx + ".repository"), p(ctx + ".client")));
            r.put(ctx + ": repository só acessado pelo service do contexto",
                    classes().that().resideInAPackage(p(ctx + ".repository"))
                            .should().onlyBeAccessed().byAnyPackage(p(ctx + ".service"), p(ctx + ".repository")));
            r.put(ctx + ": client só acessado pelo service do contexto",
                    classes().that().resideInAPackage(p(ctx + ".client"))
                            .should().onlyBeAccessed().byAnyPackage(p(ctx + ".service"), p(ctx + ".client")));
            r.put(ctx + " → " + other + " apenas via service/dto/exception",
                    noClasses().that().resideInAPackage(p(ctx))
                            .should().dependOnClassesThat().resideInAnyPackage(
                                    p(other + ".controller"), p(other + ".repository"), p(other + ".model"), p(other + ".client")));
        }
        r.put("shared não depende de contextos",
                noClasses().that().resideInAPackage(p("shared"))
                        .should().dependOnClassesThat().resideInAnyPackage(p("catalogo"), p("pedidos")));
        return r;
    }

    static Map<String, ArchRule> hexagonal() {
        Map<String, ArchRule> r = new LinkedHashMap<>();
        r.put("pacotes-permitidos", allowedPackages("domain", "application", "adapter", "config"));
        r.put("domain isolado",
                noClasses().that().resideInAPackage(p("domain"))
                        .should().dependOnClassesThat().resideInAnyPackage(concat(FRAMEWORKS, p("application"), p("adapter"), p("config"))));
        r.put("application depende só de domain",
                noClasses().that().resideInAPackage(p("application"))
                        .should().dependOnClassesThat().resideInAnyPackage(concat(FRAMEWORKS, p("adapter"), p("config"))));
        r.put("adapter.in não depende de adapter.out",
                noClasses().that().resideInAPackage(p("adapter.in"))
                        .should().dependOnClassesThat().resideInAPackage(p("adapter.out")));
        r.put("adapter.out não depende de adapter.in",
                noClasses().that().resideInAPackage(p("adapter.out"))
                        .should().dependOnClassesThat().resideInAPackage(p("adapter.in")));
        r.put("adapters usam portas, não services",
                noClasses().that().resideInAPackage(p("adapter"))
                        .should().dependOnClassesThat().resideInAPackage(p("application.service")));
        return r;
    }

    static Map<String, ArchRule> clean() {
        Map<String, ArchRule> r = new LinkedHashMap<>();
        r.put("pacotes-permitidos", allowedPackages("entity", "usecase", "adapter", "infrastructure"));
        r.put("entity não depende de anéis externos",
                noClasses().that().resideInAPackage(p("entity"))
                        .should().dependOnClassesThat().resideInAnyPackage(concat(FRAMEWORKS, p("usecase"), p("adapter"), p("infrastructure"))));
        r.put("usecase depende só de entity",
                noClasses().that().resideInAPackage(p("usecase"))
                        .should().dependOnClassesThat().resideInAnyPackage(concat(FRAMEWORKS, p("adapter"), p("infrastructure"))));
        r.put("adapter não depende de infrastructure",
                noClasses().that().resideInAPackage(p("adapter"))
                        .should().dependOnClassesThat().resideInAPackage(p("infrastructure")));
        return r;
    }

    private static String[] concat(String[] base, String... more) {
        String[] all = java.util.Arrays.copyOf(base, base.length + more.length);
        System.arraycopy(more, 0, all, base.length, more.length);
        return all;
    }

    private static String json(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ") + "\"";
    }
}
