package uk.gov.hmcts;


import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * A simple unit test for the 'uk.gov.hmcts.greeting' plugin.
 */
public class JavaPluginTest {
    @Ignore
    @Test
    public void pluginRegistersATask() {
        // Create a test project and apply the plugin
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("uk.gov.hmcts.java");

        assertNotNull(project.getTasks().findByName("checkstyleMain"));
    }
}
