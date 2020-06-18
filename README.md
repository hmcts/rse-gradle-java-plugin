# HMCTS Gradle Java Plugin ![Java CI](https://github.com/hmcts/gradle-java-plugin/workflows/Java%20CI/badge.svg) ![GitHub tag (latest SemVer)](https://img.shields.io/github/v/tag/hmcts/gradle-java-plugin?label=release)

Applies and configures a standard set of code analysis tools for use in HMCTS Java projects.

This plugin is intended to answer:

* Which static analysis tools should my HMCTS Java project be using?
* How should they be configured?


## Usage

Apply in your plugins block in `build.gradle`:

```groovy
plugins {
    id 'uk.gov.hmcts.java' version '0.8.0'
}
```

## Checkstyle

[Checkstyle](https://checkstyle.org/checks.html) is automatically applied and configured to use the [HMCTS styleguide](https://github.com/hmcts/gradle-java-plugin/blob/master/src/main/resources/hmcts-checkstyle.xml).

The checkstyle configuration is extracted to `build/config/checkstyle.xml` when checkstyle is invoked.

### Suppressing checkstyle

Checkstyle can be selectively disabled for a block using comment tags:

```java
//CHECKSTYLE:OFF
private String Violating_STYLEGUIDE_lIterAL = "bar";
//CHECKSTYLE:ON
```

## OWASP Dependency Checker

The [OWASP Dependency Checker](https://jeremylong.github.io/DependencyCheck/dependency-check-gradle/index.html) provides monitoring of the project's dependencies and creates a report of known vulnerable components that are included in the build.

By default, a set of known non-runtime dependency sets are excluded from analysis ([viewable here](https://github.com/hmcts/gradle-java-plugin/blob/f64ea895d880cc9c066a3bdef20c0fb90322cfad/src/main/java/uk/gov/hmcts/DependencyCheckSetup.java#L28-L28)).

### Usage

`./gradlew dependencyCheckAggregate`

### Suppressions

Due to the way the dependency checker works, false positives are an [expected occurence.](https://jeremylong.github.io/DependencyCheck/general/suppression.html)

Provide the dependency checker with the path to your [suppression file](https://jeremylong.github.io/DependencyCheck/general/suppression.html):

```groovy
dependencyCheck {
  suppressionFile = 'path/to/supression.xml'
}
```

### Cleaning unnecessary suppressions

A `cleanSuppressions` gradle task is provided that removes any obsolete suppressions from your dependency checker suppression file.

`./gradlew cleanSuppressions`

Will run the dependency checker's dependency analysis and compare the detected CVEs to your suppressions. Any suppressions that are no longer needed will be stripped from your suppressions XML file.
