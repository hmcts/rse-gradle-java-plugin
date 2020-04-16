package uk.gov.hmcts

import org.gradle.api.plugins.quality.PmdExtension
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class Test extends Specification {

    def "Configures default HMCTS PMD ruleset"() {
        when:
        def project = ProjectBuilder.builder().build()
        project.getPlugins().apply("uk.gov.hmcts.java")
        def extension = project.getExtensions().findByType(PmdExtension.class)

        then:
        !extension.ruleSetFiles.isEmpty()
    }
}
