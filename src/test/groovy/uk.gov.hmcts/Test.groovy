package uk.gov.hmcts

import spock.lang.Specification
import uk.gov.hmcts.tools.DependencyCheckSetup

class Test extends Specification {

    def "extracts set of CVEs from suppression report"() {
        given:
        def xml = this.getClass().getResource( '/dependency_checker_report_for_redundant_suppressions.xml' ).text

        when:
        def cves = DependencyCheckSetup.getSuppressedCves(xml)

        then:
        cves == [
                "CVE-2018-1258",
                "CVE-2020-10683",
                "CVE-2020-13692",
                "CVE-2020-5407",
                "CVE-2020-5408",
                "CVE-2020-9484",
                "CVE-2020-9488"
        ].toSet()
    }

    def "filters out unused suppressions"() {
        given:
        def xml = this.getClass().getResource( '/has_redundant_suppressions.xml' ).text
        def cves = [
                "CVE-2018-1258"
        ]
        when:
        def suppressions = DependencyCheckSetup.stripUnusedSuppressions(xml, cves)

        then:
        // See the suppressions file which has 4 suppressions referencing CVE-2018-1258.
        suppressions.children().size() == 4
    }
}
