package uk.gov.hmcts.tools;

import lombok.SneakyThrows;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.plugins.quality.CheckstyleExtension;
import org.gradle.api.plugins.quality.CheckstylePlugin;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.VersionNumber;

import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Scanner;
import javax.inject.Inject;

public class CheckstyleSetup extends DefaultTask {

    File configFile;

    // Recent checkstyle versions flag an annotation array indentation which is widely used at HMCTS.
    // Consequently we don't force recent versions onto teams (but they may opt in).
    public static final String minCheckstyleVersion = "8.31";

    public static void apply(Project project) {
        project.getPlugins().apply(CheckstylePlugin.class);
        CheckstyleExtension ext = project.getExtensions().getByType(CheckstyleExtension.class);
        ext.setMaxWarnings(0);
        ext.setMaxErrors(0);
        ext.setIgnoreFailures(false);

        project.afterEvaluate(evaluatedProject -> {
            CheckstyleSetup writer = project.getTasks().create("writeCheckstyleConfig",
                CheckstyleSetup.class);

            for (Checkstyle checkstyleTask : project.getTasks().withType(Checkstyle.class)) {
                if (checkstyleTask.getConfigFile() == null || !checkstyleTask.getConfigFile().exists()) {
                    // If using bundled checkstyle config, set a floor for checkstyle version since older versions may
                    // not support our bundled config.
                    if (VersionNumber.parse(minCheckstyleVersion)
                        .compareTo(VersionNumber.parse(ext.getToolVersion())) > 0) {
                        ext.setToolVersion(minCheckstyleVersion);
                    }

                    checkstyleTask.setConfigFile(writer.configFile);
                    checkstyleTask.dependsOn(writer);
                }
            }
        });
    }


    @Inject
    public CheckstyleSetup() {
        super();
        File dir = new File(getProject().getBuildDir(), "config/checkstyle");
        configFile = new File(dir, "checkstyle.xml");
    }

    @TaskAction
    @SneakyThrows
    public void writeConfig() {
        configFile.getParentFile().mkdirs();
        try (InputStream is = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("hmcts-checkstyle.xml")) {
            try (Scanner s = new Scanner(is).useDelimiter("\\A")) {
                try (BufferedWriter writer = Files.newBufferedWriter(configFile.toPath())) {
                    writer.write(s.next());
                }
            }
        }
    }
}
