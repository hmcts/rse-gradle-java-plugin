# HMCTS Gradle Java Plugin ![Java CI](https://github.com/hmcts/gradle-java-plugin/workflows/Java%20CI/badge.svg) ![GitHub tag (latest SemVer)](https://img.shields.io/github/v/tag/hmcts/gradle-java-plugin?label=release)


Applies HMCTS standards to Java projects, applying code analysis tools and configuring them with default HMCTS settings.

## Usage

Apply the plugin in your plugins block in `build.gradle`:

```groovy
plugins {
    id 'uk.gov.hmcts.java' version '0.3.1'
}
```

## [Checkstyle](https://checkstyle.org/checks.html)

Checkstyle is automatically applied and configured to use the [HMCTS styleguide](https://github.com/hmcts/gradle-java-plugin/blob/master/src/main/resources/hmcts-checkstyle.xml).

The HMCTS styleguide is extracted to `build/config/checkstyle.xml` when checkstyle is invoked.

### Checkstyle exclusions

Checkstyle can be selectively disabled for a block using comment tags:

```java
//CHECKSTYLE:OFF
private String Violating_STYLEGUIDE_lIterAL = "bar";
//CHECKSTYLE:ON
```

## [PMD](https://pmd.github.io/pmd-6.22.0/)

PMD is applied with the following [ruleset](https://github.com/hmcts/gradle-java-plugin/blob/master/src/main/resources/pmd-ruleset.xml), which is extracted to `build/config/pmd-ruleset.xml` when pmd is invoked.

## [OWASP Dependency Checker](https://jeremylong.github.io/DependencyCheck/dependency-check-gradle/index.html)

Provides monitoring of the project's dependencies and creates a report of known vulnerable components that are included in the build. By default, a set of known non-runtime dependency sets are excluded from analysis ([viewable here](https://github.com/hmcts/gradle-java-plugin/blob/f64ea895d880cc9c066a3bdef20c0fb90322cfad/src/main/java/uk/gov/hmcts/DependencyCheckSetup.java#L28-L28)).

The checker is invoked with `gradle dependencyCheckAnalyze`

### Suppressions

Provide the dependency checker with the path to your [suppression file](https://jeremylong.github.io/DependencyCheck/general/suppression.html):

```groovy
dependencyCheck {
  suppressionFile = 'path/to/supression.xml'
}
```
