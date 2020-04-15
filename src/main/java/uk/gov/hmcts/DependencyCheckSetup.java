package uk.gov.hmcts;

import java.util.Arrays;

import org.gradle.api.Project;
import org.owasp.dependencycheck.gradle.DependencyCheckPlugin;
import org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension;

final class DependencyCheckSetup {
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

        // Exclude scanning of these known non-runtime dependency sets.
        extension.setSkipConfigurations(Arrays.asList(
            "checkstyle",
            "compileOnly",
            "pmd",
            "integrationTest",
            "functionalTest",
            "smokeTest"
        ));
    }
}
