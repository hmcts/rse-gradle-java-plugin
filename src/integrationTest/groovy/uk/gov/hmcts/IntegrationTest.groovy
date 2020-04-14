package uk.gov.hmcts

import com.google.common.collect.Lists
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class IntegrationTest extends Specification {
    @Rule
    TemporaryFolder tempFolder = new TemporaryFolder()
    File buildFile

    void setup() {
        buildFile = tempFolder.newFile('build.gradle')
    }

    def "Can run checkstyle"() {
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
        def result = runner("checkStyleFunctionalTest")
            .build()

        then:
        result.output.contains("Task :checkstyleFunctionalTest NO-SOURCE")
    }

   def  "Can run PMD"() {
       given:
       buildFile << """
            plugins {
                id 'java-library'
                id 'uk.gov.hmcts.java'
            }
        """
       when:
        def result = runner("pmdMain")
            .build()

       then:
       result.output.contains("pmd")
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
        // Expect build failure or success
        def result = runner((["dependencyCheckAnalyze"] + args) as String[])
            ."$buildResult"()

        then:
        result.output.contains("dependencies were identified with known vulnerabilities")

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
            .withProjectDir(tempFolder.getRoot())
    }
}
