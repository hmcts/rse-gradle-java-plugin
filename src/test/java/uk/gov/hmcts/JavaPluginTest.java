package uk.gov.hmcts;


import static org.assertj.core.api.Assertions.assertThat;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Ignore;
import org.junit.Test;

public class JavaPluginTest {
    @Ignore
    @Test
    public void pluginRegistersATask() {
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("uk.gov.hmcts.java");

        assertThat(project.getTasks().findByName("checkstyleMain"))
            .isNotNull();

    }
}
