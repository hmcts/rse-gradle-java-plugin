package uk.gov.hmcts

import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.gov.hmcts.tools.CheckstyleSetup

// Test plugin against a complete Java library project.
class EndToEndTest extends Specification {
    @Rule
    TemporaryFolder projectFolder = new TemporaryFolder()

    def setup() {
        File testLibrary = new File("test-projects/test-library")
        FileUtils.copyDirectory(testLibrary, projectFolder.getRoot())
    }

    def "All tasks run successfully"() {
        when:
        def result = GradleRunner.create()
                .forwardOutput()
                .withPluginClasspath()
                .withArguments("check", "cleanSuppressions", "-is", "assertRepositoriesOrdered")
                .withGradleVersion(gradleVersion)
                .withProjectDir(projectFolder.getRoot())
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
               "5.0",
               "6.0"
        ]
    }

    def "Cleans unused dependency suppressions"() {
        when:
        GradleRunner.create()
                .forwardOutput()
                .withDebug(true)
                .withPluginClasspath()
                .withArguments("cleanSuppressions", "-is")
                .withGradleVersion("5.0")
                .withProjectDir(projectFolder.getRoot())
                .build()

        def cleanSuppressions = new File(projectFolder.root, "suppressions.xml")
        def doc = new XmlSlurper().parse(cleanSuppressions)

        then:
        // The fake suppression should be cleared out.
        doc.children().size() == 0
    }
}
