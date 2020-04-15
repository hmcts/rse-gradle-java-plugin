package uk.gov.hmcts

import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class FunctionalTest extends Specification {
    @Rule
    TemporaryFolder projectFolder = new TemporaryFolder()

    def setup() {
        File testLibrary = new File("test-projects/test-library")
        FileUtils.copyDirectory(testLibrary, projectFolder.getRoot())
    }

    def "Runs all checks"() {
        when:
        def result = GradleRunner.create()
                .forwardOutput()
                .withPluginClasspath()
                .withArguments("check", "-is")
                .withGradleVersion("4.10.3")
                .withProjectDir(projectFolder.getRoot())
                .build()

        then:
        result.output.contains(":checkstyleMain")
        result.output.contains(":pmdMain")
        new File(projectFolder.getRoot(), "build/reports/pmd/main.html").exists()
    }
}