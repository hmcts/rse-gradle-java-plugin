package uk.gov.hmcts;

import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class JavaPluginFunctionalTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void before() throws IOException {
        File testLibrary = new File("test-projects/test-library");
        System.out.println(testLibrary.getAbsolutePath());
        FileUtils.copyDirectory(testLibrary, tempFolder.getRoot());
    }

    @Test
    public void canRunCheckstyle() {
        BuildResult result = runner()
            .withArguments("checkStyleMain", "-is")
            .buildAndFail();

        assertThat(result.getOutput()).contains("Checkstyle files with violations: 1");
    }

    @Test
    public void canRunPmd() {
        runner()
            .withArguments("pmdMain", "-is")
            .build();
    }

    GradleRunner runner() {
        return GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withGradleVersion("4.10.3")
            .withProjectDir(tempFolder.getRoot());
    }
}
