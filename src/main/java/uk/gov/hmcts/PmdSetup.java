package uk.gov.hmcts;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
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
    public static void apply(Project project) {
        project.getPlugins().apply(PmdPlugin.class);
        PmdExtension pmd = project.getExtensions().getByType(PmdExtension.class);
        pmd.setToolVersion("6.18.0");
        pmd.setReportsDir(project.getBuildDir().toPath().resolve("reports/pmd").toFile());
        pmd.setIgnoreFailures(false);
        // https://github.com/pmd/pmd/issues/876
        pmd.setRuleSets(new ArrayList<>());

        project.afterEvaluate(PmdSetup::configurePmd);
    }

    // This must be done after Gradle's initialisation phase
    // since it requires PMD's tasks to have been created based on source sets.
    private static void configurePmd(Project project) {
        PmdSetup writer = project.getTasks().create("writePMDConfig",
            PmdSetup.class);

        PmdExtension pmd = project.getExtensions().getByType(PmdExtension.class);
        if (pmd.getRuleSets().isEmpty()) {
            pmd.ruleSetFiles(writer.configFile);
        }

        for (Pmd pmdTask : project.getTasks().withType(Pmd.class)) {
            pmdTask.dependsOn(writer);
        }
    }

    File configFile;

    @Inject
    public PmdSetup() {
        File dir = new File(getProject().getBuildDir(), "config/pmd");
        configFile = new File(dir, "ruleset.xml");
    }

    @TaskAction
    @SneakyThrows
    public void writeConfig() {
        configFile.getParentFile().mkdirs();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("pmd-ruleset.xml")) {
            Scanner s = new Scanner(is).useDelimiter("\\A");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
                writer.write(s.next());
            }
        }
    }
}
