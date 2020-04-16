package uk.gov.hmcts.tools;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;

import lombok.SneakyThrows;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.plugins.quality.Pmd;
import org.gradle.api.plugins.quality.PmdExtension;
import org.gradle.api.plugins.quality.PmdPlugin;
import org.gradle.api.tasks.TaskAction;

public class PmdSetup extends DefaultTask {

    File configFile;

    public static void apply(Project project) {
        project.getPlugins().apply(PmdPlugin.class);

        PmdExtension pmd = project.getExtensions().getByType(PmdExtension.class);
        pmd.setToolVersion("6.18.0");
        pmd.setReportsDir(project.getBuildDir().toPath().resolve("reports/pmd").toFile());
        pmd.setIgnoreFailures(false);
        // https://github.com/pmd/pmd/issues/876
        pmd.setRuleSets(new ArrayList<>());

        PmdSetup configWriter = project.getTasks().create("writePMDConfig",
            PmdSetup.class);
        pmd.ruleSetFiles(configWriter.configFile);

        // Once PMD tasks are created they must depend on PMD config being written.
        project.afterEvaluate(evaluatedProject -> {
            for (Pmd pmdTask : project.getTasks().withType(Pmd.class)) {
                pmdTask.dependsOn(configWriter);
            }
        });
    }

    @Inject
    public PmdSetup() {
        super();
        File dir = new File(getProject().getBuildDir(), "config/pmd");
        configFile = new File(dir, "pmd-ruleset.xml");
    }

    @TaskAction
    @SneakyThrows
    public void writeConfig() {
        configFile.getParentFile().mkdirs();
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("pmd-ruleset.xml")) {
            try (Scanner s = new Scanner(is).useDelimiter("\\A")) {
                try (BufferedWriter writer = Files.newBufferedWriter(configFile.toPath())) {
                    writer.write(s.next());
                }
            }
        }
    }
}
