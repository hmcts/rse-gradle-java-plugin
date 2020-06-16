package uk.gov.hmcts.tools;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import groovy.util.slurpersupport.NodeChild;
import lombok.SneakyThrows;
import org.gradle.api.Project;
import org.owasp.dependencycheck.gradle.DependencyCheckPlugin;
import org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension;

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
}
