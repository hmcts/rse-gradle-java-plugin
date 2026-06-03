package uk.gov.hmcts.tools;

import lombok.SneakyThrows;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.plugins.quality.CheckstyleExtension;
import org.gradle.api.plugins.quality.CheckstylePlugin;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.work.DisableCachingByDefault;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Scanner;

@DisableCachingByDefault(because = "Writes a generated Checkstyle configuration file from bundled plugin resources")
public class CheckstyleSetup extends DefaultTask {

    private final File configFile;

    // Recent checkstyle versions flag an annotation array indentation which is widely used at HMCTS.
    // Consequently, we don't force recent versions onto teams (but they may opt in).
    public static final ComparableVersion minCheckstyleVersion = new ComparableVersion("8.31");

    public static void apply(Project project) {
        project.getPlugins().apply(CheckstylePlugin.class);

        CheckstyleExtension ext = project.getExtensions().getByType(CheckstyleExtension.class);
        ext.setMaxWarnings(0);
        ext.setMaxErrors(0);
        ext.setIgnoreFailures(false);

        TaskProvider<CheckstyleSetup> writer = project.getTasks().register(
                "writeCheckstyleConfig",
                CheckstyleSetup.class
        );

        project.afterEvaluate(evaluatedProject ->
                evaluatedProject.getTasks().withType(Checkstyle.class).configureEach(checkstyleTask -> {
                    if (checkstyleTask.getConfigFile() == null || !checkstyleTask.getConfigFile().exists()) {
                        ComparableVersion currentCheckStyleVersion = new ComparableVersion(ext.getToolVersion());

                        // If using bundled checkstyle config, set a floor for checkstyle version since older versions
                        // may not support our bundled config.
                        if (minCheckstyleVersion.compareTo(currentCheckStyleVersion) > 0) {
                            ext.setToolVersion(minCheckstyleVersion.toString());
                        }

                        checkstyleTask.setConfigFile(writer.get().getConfigFile());
                        checkstyleTask.dependsOn(writer);
                    }
                })
        );
    }

    @Inject
    public CheckstyleSetup() {
        File dir = getProject().getLayout().getBuildDirectory().dir("config/checkstyle").get().getAsFile();
        this.configFile = new File(dir, "checkstyle.xml");
    }

    @OutputFile
    public File getConfigFile() {
        return configFile;
    }

    @TaskAction
    @SneakyThrows
    public void writeConfig() {
        configFile.getParentFile().mkdirs();

        try (InputStream is = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("hmcts-checkstyle.xml")) {

            try (Scanner scanner = new Scanner(is).useDelimiter("\\A");
                BufferedWriter writer = Files.newBufferedWriter(configFile.toPath())) {
                writer.write(scanner.next());
            }
        }
    }
}
