package uk.gov.hmcts;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class JavaPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        CheckstyleSetup.apply(project);
        PmdSetup.apply(project);
        DependencyCheckSetup.apply(project);
    }
}
