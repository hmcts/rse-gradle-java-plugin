package uk.gov.hmcts;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class JavaPluginFunctionalTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void before() throws IOException {
        File testLibrary = new File("test-projects/test-library");
        FileUtils.copyDirectory(testLibrary, tempFolder.getRoot());
    }

    @Test
    public void canRunCheckstyle() {
        BuildResult result = runner("checkStyleMain")
            .build();

        assertThat(result.getOutput()).contains("Running Checkstyle");
    }

    @Test
    public void canRunPmd() {
        BuildResult result = runner("pmdMain")
            .build();

        assertThat(result.getOutput()).contains("pmd");
    }

    @Test
    public void dependencyCheckCanFailBuild() {
        BuildResult result = runner("dependencyCheckAnalyze", "-DdependencyCheck.failBuild=true")
            .buildAndFail();

        assertThat(result.getOutput()).contains("dependencies were identified with known vulnerabilities");
    }

    @Test
    public void dependencyCheckCanAllowBuild() {
        BuildResult result = runner("dependencyCheckAnalyze")
            .build();

        assertThat(result.getOutput()).contains("dependencies were identified with known vulnerabilities");
    }

    GradleRunner runner(String... args) {
        ArrayList<String> arguments = Lists.newArrayList(args);
        arguments.add("-is");
        return GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withArguments(arguments)
            .withGradleVersion("4.10.3")
            .withProjectDir(tempFolder.getRoot());
    }
}
