package uk.gov.hmcts

import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir

// Test plugin against a complete Java library project.
class EndToEndTest extends Specification {
    @TempDir
    File projectFolder;

    def setup() {
        File testLibrary = new File("test-projects/test-library")
        FileUtils.copyDirectory(testLibrary, projectFolder)
    }

    def "All tasks run successfully"() {
        when:
        def result = GradleRunner.create()
                .forwardOutput()
                .withPluginClasspath()
                .withArguments("check", "cleanSuppressions", "-is", "assertRepositoriesOrdered")
                .withGradleVersion(gradleVersion)
                .withProjectDir(projectFolder)
                .build()

        then:
        result.taskPaths(TaskOutcome.SUCCESS).containsAll(
                ":checkstyleMain",
                ":cleanSuppressions",
                ":assertRepositoriesOrdered"
        )
        // The test project declares checkstyle 8.32
        result.output =~ "Running Checkstyle 8.32"

        where:
        gradleVersion << [
               "8.3"
        ]
    }

    def "Cleans unused dependency suppressions"() {
        when:
        GradleRunner.create()
                .forwardOutput()
                .withDebug(true)
                .withPluginClasspath()
                .withArguments("cleanSuppressions", "-is")
                .withGradleVersion("8.3")
                .withProjectDir(projectFolder)
                .build()

        def cleanSuppressions = new File(projectFolder, "suppressions.xml")
        def doc = new XmlSlurper().parse(cleanSuppressions)

        then:
        // The fake suppression should be cleared out.
        doc.children().size() == 0
    }
}
