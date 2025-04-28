package uk.gov.hmcts

import com.google.common.collect.Lists
import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir

class IntegrationTest extends Specification {
    @TempDir
    File projectFolder;
    File buildFile

    void setup() {
        buildFile = new File(projectFolder, 'build.gradle')
    }

    def "Check runs checkstyle against all sourcesets"() {
        given:
        buildFile << """
            plugins {
                id 'java-library'
                id 'uk.gov.hmcts.java'
            }
            sourceSets {
                functionalTest {
                }
            }
        """
        when:
        def taskPaths = runner("check")
            .build()
            .taskPaths(TaskOutcome.NO_SOURCE)

        then:
        taskPaths.containsAll(
                ":checkstyleMain",
                ":checkstyleFunctionalTest"
        )
    }

    def "Dependency check excludes known non-runtime configurations"() {
        given:
        buildFile << """
            plugins {
                id 'java-library'
                id 'uk.gov.hmcts.java'
            }
            sourceSets {
                integrationTest {
                }
                functionalTest {
                }
                smokeTest {
                }
            }
        """
        when:
        // Expect build failure or success depending on provided Gradle property.
        def result = runner("dependencyCheckAnalyze")
                .build()

        then:
        result.output =~ "Analyzing.+:runtimeClasspath\\s"
        !(result.output =~ "Analyzing.+:integrationTest\\s")
        !(result.output =~ "Analyzing.+:functionalTest\\s")
        !(result.output =~ "Analyzing.+:smokeTest\\s")
        !(result.output =~ "Analyzing.+:pmd\\s")
        !(result.output =~ "Analyzing.+:checkstyle\\s")
        !(result.output =~ "Analyzing.+:compileOnly\\s")
    }

    def "Dependency check fails build by default"() {
        given:
        buildFile << """
            plugins {
                id 'java-library'
                id 'uk.gov.hmcts.java'
            }
            repositories {
                mavenCentral()
            }

            dependencies {
                // Known to have a CVE that should be detected by dependency checker.
                compile group: 'com.fasterxml.jackson.core', name: 'jackson-databind', version: '2.7.0'
            }
        """
        when:
        // Expect build failure or success depending on provided Gradle property.
        def result = runner((["dependencyCheckAnalyze"]) as String[])
            .buildAndFail()

        then:
        result.output.contains("dependencies were identified with known vulnerabilities")
        new File(projectFolder, 'build/reports/dependency-check-report.html').exists()
    }

    def "Dependency check detects vulnerabilities in transient dependencies"() {
        given:
        File testLibrary = new File("test-projects/test-library")
        FileUtils.copyDirectory(testLibrary, new File(projectFolder, "test-library"))
        buildFile << """
            plugins {
                id 'java-library'
                id 'uk.gov.hmcts.java'
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                // Known to have a CVE that should be detected by dependency checker.
                compile group: 'org.springframework.cloud', name: 'spring-cloud-cloudfoundry-connector', version: '1.2.9.RELEASE'
            }
        """
        new File(projectFolder, 'settings.gradle') << """
            include 'test-library'
        """

        when:
        def result = runner((["dependencyCheckAnalyze"]) as String[])
                .buildAndFail()

        then:
        result.output.contains("dependencies were identified with known vulnerabilities")
    }

    GradleRunner runner(String... args) {
        ArrayList<String> arguments = Lists.newArrayList(args)
        arguments.add("-is")
        return GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withArguments(arguments)
            .withGradleVersion("8.3")
            .withProjectDir(projectFolder)
    }
}
