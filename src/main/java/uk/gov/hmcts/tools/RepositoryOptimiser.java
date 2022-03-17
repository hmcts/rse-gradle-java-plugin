package uk.gov.hmcts.tools;

import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.ArtifactRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;


public class RepositoryOptimiser {

    private static final HashMap<String, Integer> REPO_PRIORITY = new HashMap<String, Integer>() {{
            put("MavenLocal", 0);
            put("MavenRepo", 1);
            put("maven", 2);
        }};

    private RepositoryOptimiser() {
    }

    public static void apply(Project project) {

        project.afterEvaluate(evaluatedProject -> {

            var repositoryHandler = evaluatedProject.getRepositories();

            ArtifactRepository[] repos = new ArtifactRepository[repositoryHandler.size()];
            repos = repositoryHandler.toArray(repos);
            Arrays.sort(repos, (a, b) -> {
                if (REPO_PRIORITY.containsKey(a.getName()) && REPO_PRIORITY.containsKey(b.getName())) {
                    return REPO_PRIORITY.get(a.getName()) < REPO_PRIORITY.get(b.getName()) ? -1 : 1;
                } else if (REPO_PRIORITY.containsKey(a.getName())) {
                    return -1;
                } else if (REPO_PRIORITY.containsKey(b.getName())) {
                    return 1;
                }
                return 0;
            });

            repositoryHandler.clear();
            Collections.addAll(repositoryHandler, repos);
        });
    }
}
