package uk.gov.hmcts.tools;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import groovy.util.Node;
import groovy.util.XmlParser;
import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import groovy.util.slurpersupport.NodeChild;
import groovy.xml.XmlUtil;
import lombok.SneakyThrows;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.owasp.dependencycheck.gradle.DependencyCheckPlugin;
import org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension;
import org.owasp.dependencycheck.reporting.ReportGenerator.Format;

public final class DependencyCheckSetup {
    static final List<String> NON_RUNTIME_CONFIGURATIONS = Arrays.asList(
        "checkstyle",
        "compileOnly",
        "pmd",
        "integrationTest",
        "functionalTest",
        "smokeTest");

    static final List<String> VARIANTS = Arrays.asList(
        "Compile",
        "CompileClasspath",
        "CompileOnly",
        "Runtime",
        "RuntimeClasspath");

    private DependencyCheckSetup() {
    }

    public static void apply(Project project) {
        project.getPlugins().apply(DependencyCheckPlugin.class);

        // Specifies if the build should be failed if a CVSS score above a specified level is identified.
        // range of 0-10 fails the build, anything greater and it doesn't fail the build.
        int code = "true".equalsIgnoreCase(System.getProperty("dependencyCheck.failBuild"))
            ? 0
            : 11;
        DependencyCheckExtension extension = project.getExtensions().getByType(DependencyCheckExtension.class);
        extension.setFailBuildOnCVSS((float) code);

        // Disable scanning of .NET related binaries
        extension.getAnalyzers().setAssemblyEnabled(false);

        // Exclude non-runtime configurations by default.
        // This can be overridden in project build script if desired.
        List<String> skip = extension.getSkipConfigurations();
        skip.addAll(NON_RUNTIME_CONFIGURATIONS);
        for (String configuration : NON_RUNTIME_CONFIGURATIONS) {
            for (String variant : VARIANTS) {
                skip.add(configuration + variant);
            }
        }

        extension.getFormats().add(Format.XML);

        Task cleaner = project.getTasks().create("cleanSuppressions");
        cleaner.dependsOn("dependencyCheckAggregate");
        cleaner.doLast(x -> {
            File reportDir = project.file(extension.getOutputDirectory());
            File report = new File(reportDir, "dependency-check-report.xml");
            Set<String> cves = getSuppressedCves(readFile(report));
            File suppressions = project.file(extension.getSuppressionFile());
            Node cleanedReport = stripUnusedSuppressions(readFile(suppressions), cves);
            writeFile(suppressions, XmlUtil.serialize(cleanedReport));
        });
    }

    @SneakyThrows
    private static void writeFile(File f, String content) {
        try (FileWriter w = new FileWriter(f)) {
            w.write(content);
        }
    }

    @SneakyThrows
    public static Set<String> getSuppressedCves(String dependencyCheckerReport) {
        GPathResult response = new XmlSlurper().parseText(dependencyCheckerReport);
        Set<String> result = new HashSet<>();
        response.depthFirst().forEachRemaining(x -> {
            NodeChild n = (NodeChild) x;
            if (n.name().equals("suppressedVulnerability")) {
                result.add(n.getProperty("name").toString());
            }
        });
        return result;
    }

    @SneakyThrows
    public static Node stripUnusedSuppressions(String suppressionXml, Collection<String> usedCves) {
        Node suppressions = new XmlParser().parseText(suppressionXml);

        List<Node> redundant = new ArrayList<>();
        suppressions.children().forEach(x -> {
            Node n = (Node) x;
            if (!usedCves.stream().anyMatch(c -> n.text().contains(c))) {
                redundant.add(n);
            }
        });
        redundant.forEach(x -> suppressions.remove(x));

        return suppressions;
    }

    @SneakyThrows
    static String readFile(File f) {
        byte[] encoded = Files.readAllBytes(f.toPath());
        return new String(encoded, StandardCharsets.UTF_8);
    }
}
