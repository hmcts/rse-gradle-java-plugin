package uk.gov.hmcts;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import uk.gov.hmcts.tools.DependencyCheckSetup;
import uk.gov.hmcts.tools.CheckstyleSetup;
import uk.gov.hmcts.tools.RepositoryOptimiser;

public class JavaPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        CheckstyleSetup.apply(project);
        DependencyCheckSetup.apply(project);
        RepositoryOptimiser.apply(project);
    }
}
