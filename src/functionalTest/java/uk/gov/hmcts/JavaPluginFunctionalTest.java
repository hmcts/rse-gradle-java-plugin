package uk.gov.hmcts;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

public class JavaPluginFunctionalTest {
    @Test public void canRunTask() throws IOException {
        File projectDir = new File("test-projects/test-library");
        Files.createDirectories(projectDir.toPath());

        GradleRunner runner = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withArguments("check", "-is", "--rerun-tasks")
            .withProjectDir(projectDir);
        BuildResult result = runner.buildAndFail();

        assertThat(result.getOutput()).contains("Checkstyle files with violations: 1");
    }
}
