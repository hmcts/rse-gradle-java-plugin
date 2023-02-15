package uk.gov.hmcts.tools;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import groovy.util.slurpersupport.NodeChild;
import groovy.xml.DOMBuilder;
import groovy.xml.XmlUtil;
import lombok.SneakyThrows;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.owasp.dependencycheck.gradle.DependencyCheckPlugin;
import org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension;
import org.owasp.dependencycheck.reporting.ReportGenerator.Format;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class DependencyCheckSetup {

    private DependencyCheckSetup() {
    }

    public static void apply(Project project) {
        project.getPlugins().apply(DependencyCheckPlugin.class);

        DependencyCheckExtension extension = project.getExtensions().getByType(DependencyCheckExtension.class);
        extension.setFailBuildOnCVSS(0f);

        // Match the CNP pipeline which disables these checks
        // https://github.com/hmcts/cnp-jenkins-library/blob/master/src/uk/gov/hmcts/contino/GradleBuilder.groovy#L135
        var analyzers = extension.getAnalyzers();
        analyzers.setAssemblyEnabled(false);
        analyzers.setCentralEnabled(false);
        analyzers.getRetirejs().setEnabled(false);
        analyzers.getOssIndex().setEnabled(false);


        // Scan only runtime configurations by default.
        // This can be overridden in project build script if desired.
        extension.getScanConfigurations().add("runtimeClasspath");

        extension.getFormats().add(Format.XML.name());
        Task cleaner = project.getTasks().create("cleanSuppressions");
        cleaner.dependsOn("dependencyCheckAggregate");
        cleaner.doLast(x -> {
            File reportDir = project.file(extension.getOutputDirectory());
            File report = new File(reportDir, "dependency-check-report.xml");
            Set<String> cves = getSuppressedCves(readFile(report));
            File suppressions = project.file(extension.getSuppressionFile());
            String cleanedReport = stripUnusedSuppressions(readFile(suppressions), cves);
            writeFile(suppressions, cleanedReport);
        });
    }

    @SneakyThrows
    public static Set<String> getSuppressedCves(String dependencyCheckerReport) {
        GPathResult response = new XmlSlurper().parseText(dependencyCheckerReport);
        Set<String> result = new HashSet<>();
        response.depthFirst().forEachRemaining(x -> {
            NodeChild n = (NodeChild) x;
            if (n.name().equals("suppressedVulnerability")) {
                result.add(n.getProperty("name").toString());
            }
        });
        return result;
    }

    @SneakyThrows
    public static String stripUnusedSuppressions(String suppressionXml, Collection<String> usedCves) {
        Element suppressions = DOMBuilder.parse(new StringReader(suppressionXml)).getDocumentElement();

        // Remove any unused CVEs.
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPathExpression xpathExp = xpathFactory.newXPath().compile(
            "//*[local-name()='cve']");
        NodeList cves = (NodeList)
            xpathExp.evaluate(suppressions, XPathConstants.NODESET);
        for (int i = 0; i < cves.getLength(); i++) {
            Node cve = cves.item(i);
            if (!usedCves.stream().anyMatch(c -> cve.getTextContent().contains(c))) {
                cve.getParentNode().removeChild(cve);
            }
        }

        // Remove any unused suppressions.
        for (int t = 0; t < suppressions.getChildNodes().getLength(); t++) {
            Node n = suppressions.getChildNodes().item(t);
            // Remove the whole node if it has no reference to active CVEs.
            if (!usedCves.stream().anyMatch(c -> n.getTextContent().contains(c))) {
                suppressions.removeChild(n);
                t--;
                continue;
            }
        }

        // Strip out all whitespace and reindent.
        // This must be done in multiple passes since removing nodes creates new whitespace.
        xpathExp = xpathFactory.newXPath().compile(
            "//text()[normalize-space(.) = '']");
        while (true) {
            NodeList emptyTextNodes = (NodeList)
                xpathExp.evaluate(suppressions, XPathConstants.NODESET);

            if (emptyTextNodes.getLength() <= 0) {
                break;
            }
            for (int i = 0; i < emptyTextNodes.getLength(); i++) {
                Node emptyTextNode = emptyTextNodes.item(i);
                emptyTextNode.getParentNode().removeChild(emptyTextNode);
            }
        }

        return XmlUtil.serialize(suppressions);
    }

    @SneakyThrows
    private static String readFile(File f) {
        byte[] encoded = Files.readAllBytes(f.toPath());
        return new String(encoded, StandardCharsets.UTF_8);
    }

    @SneakyThrows
    private static void writeFile(File f, String content) {
        try (FileWriter w = new FileWriter(f)) {
            w.write(content);
        }
    }
}
