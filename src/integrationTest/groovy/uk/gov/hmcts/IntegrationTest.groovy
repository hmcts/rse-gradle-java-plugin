package uk.gov.hmcts

import com.google.common.collect.Lists
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class IntegrationTest extends Specification {
    @Rule
    TemporaryFolder projectFolder = new TemporaryFolder()
    File buildFile

    void setup() {
        buildFile = projectFolder.newFile('build.gradle')
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
        result.output =~ "Analyzing.+:compile\\s"
        result.output =~ "Analyzing.+:runtime\\s"
        !(result.output =~ "Analyzing.+:integrationTest\\s")
        !(result.output =~ "Analyzing.+:functionalTest\\s")
        !(result.output =~ "Analyzing.+:smokeTest\\s")
        !(result.output =~ "Analyzing.+:pmd\\s")
        !(result.output =~ "Analyzing.+:checkstyle\\s")
        !(result.output =~ "Analyzing.+:compileOnly\\s")
    }

    def "Dependency check can fail build"(args, buildResult) {
        given:
        buildFile << """
            plugins {
                id 'java-library'
                id 'uk.gov.hmcts.java'
            }
            repositories {
                jcenter()
            }

            dependencies {
                // Known to have a CVE that should be detected by dependency checker.
                compile group: 'com.fasterxml.jackson.core', name: 'jackson-databind', version: '2.7.0'
            }
        """
        when:
        // Expect build failure or success depending on provided Gradle property.
        def result = runner((["dependencyCheckAnalyze"] + args) as String[])
            ."$buildResult"()

        then:
        result.output.contains("dependencies were identified with known vulnerabilities")
        new File(projectFolder.getRoot(), 'build/reports/dependency-check-report.html').exists()

        where:
        args | buildResult
        ["-DdependencyCheck.failBuild=true"] | "buildAndFail"
        [] | "build"
    }

    GradleRunner runner(String... args) {
        ArrayList<String> arguments = Lists.newArrayList(args)
        arguments.add("-is")
        return GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withArguments(arguments)
            .withGradleVersion("4.10.3")
            .withProjectDir(projectFolder.getRoot())
    }
}
