package uk.gov.hmcts.tools;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.ArtifactRepository;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;


public class RepositoryOptimiser extends DefaultTask {

    private static final String PRIORITY_REPO_NAME = "mavenlocal";

    @Inject
    public RepositoryOptimiser() {
        super();
    }

    public static void apply(Project project) {

        project.afterEvaluate(evaluatedProject -> {
            var repositoryHandler = evaluatedProject.getRepositories();

            ArtifactRepository[] repos = new ArtifactRepository[repositoryHandler.size()];
            repos = repositoryHandler.toArray(repos);
            Arrays.sort(repos, (a, b) -> a.getName().equalsIgnoreCase(PRIORITY_REPO_NAME) ? -1 : 1);

            repositoryHandler.clear();
            Collections.addAll(repositoryHandler, repos);
        });
    }
}
