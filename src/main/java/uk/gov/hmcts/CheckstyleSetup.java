package uk.gov.hmcts;

import lombok.SneakyThrows;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.plugins.quality.CheckstyleExtension;
import org.gradle.api.plugins.quality.CheckstylePlugin;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Scanner;
import javax.inject.Inject;

public class CheckstyleSetup extends DefaultTask {

    public static void apply(Project project) {
        project.getPlugins().apply(CheckstylePlugin.class);
        CheckstyleExtension ext = project.getExtensions().getByType(CheckstyleExtension.class);
        ext.setToolVersion("8.31");
        ext.setMaxWarnings(0);
        ext.setMaxErrors(0);
        ext.setIgnoreFailures(false);

        project.afterEvaluate(CheckstyleSetup::configureCheckstyleTasks);
    }

    private static void configureCheckstyleTasks(Project project) {
        CheckstyleSetup writer = project.getTasks().create("writeCheckstyleConfig",
            CheckstyleSetup.class);

        for (Checkstyle checkstyleTask : project.getTasks().withType(Checkstyle.class)) {
            if (checkstyleTask.getConfigFile() == null || !checkstyleTask.getConfigFile().exists()) {
                checkstyleTask.setConfigFile(writer.configFile);
                checkstyleTask.dependsOn(writer);
            }
        }
    }

    File configFile;

    @Inject
    public CheckstyleSetup() {
        File dir = new File(getProject().getBuildDir(), "config/checkstyle");
        configFile = new File(dir, "checkstyle.xml");
    }

    @TaskAction
    @SneakyThrows
    public void writeConfig() {
        configFile.getParentFile().mkdirs();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("hmcts-checkstyle.xml")) {
            Scanner s = new Scanner(is).useDelimiter("\\A");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
                writer.write(s.next());
            }
        }
    }
}
