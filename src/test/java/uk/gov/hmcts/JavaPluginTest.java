package uk.gov.hmcts;


import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class JavaPluginTest {
    @Ignore
    @Test
    public void pluginRegistersATask() {
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("uk.gov.hmcts.java");

        assertNotNull(project.getTasks().findByName("checkstyleMain"));
    }
}
