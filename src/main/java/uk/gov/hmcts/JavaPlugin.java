package uk.gov.hmcts;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class JavaPlugin implements Plugin<Project> {

    public void apply(Project project) {
        CheckstyleSetup.apply(project);
    }
}
