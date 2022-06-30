package uk.gov.hmcts

import spock.lang.Specification
import uk.gov.hmcts.tools.DependencyCheckSetup

class Test extends Specification {

    def "extracts set of CVEs from suppression report"() {
        given:
        def xml = this.getClass().getResource('/dependency_checker_report_for_redundant_suppressions.xml').text

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
        def xml = this.getClass().getResource('/has_redundant_suppressions.xml').text
        def cves = [
                "CVE-2018-1258"
        ]
        when:
        String report = DependencyCheckSetup.stripUnusedSuppressions(xml, cves)
        def suppressions = new groovy.util.XmlParser().parseText(report)

        then:
        // See the suppressions file which has 5 suppressions referencing CVE-2018-1258.
        suppressions.children().size() == 5
        // Preserves these data tags on reserialisation.
        report.contains "<![CDATA["

        // Individual unused CVEs should be stripped out
        suppressions.children()[0].value().size() == 2
        suppressions.children()[0].notes.text() == "preserved"
        suppressions.children()[0].cve.text() == "CVE-2018-1258"

        // Blank lines stripped from serialized file
        report.readLines().every() { !(it ==~ /^\s*$/)}
    }
}
