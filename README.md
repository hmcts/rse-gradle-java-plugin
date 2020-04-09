# HMCTS Gradle Java Plugin ![Java CI](https://github.com/hmcts/gradle-java-plugin/workflows/Java%20CI/badge.svg) ![GitHub tag (latest SemVer)](https://img.shields.io/github/v/tag/hmcts/gradle-java-plugin?label=release)


This plugin assists applying HMCTS standards to Java projects.

## Usage

Apply the plugin in your plugins block in `build.gradle`:

```groovy
plugins {
    id 'uk.gov.hmcts.java' version '0.3.1'
}
```

## Customisation

The plugin applies HMCTS standard settings for each analysis tool. These settings can be overridden if absolutely necessary by providing your own configuration blocks according to each tool's documentation:

```groovy
checkstyle {
    maxWarnings = ...
}
```

## Checkstyle

Checkstyle is automatically applied and configured to use the [HMCTS styleguide](https://github.com/hmcts/gradle-java-plugin/blob/master/src/main/resources/hmcts-checkstyle.xml).

If you need access to the checkstyle.xml file, eg. for configuring an IDE, it is extracted into a
 .config folder in your project root.

### Checkstyle Exemptions

Checkstyle can be selectively disabled for a block using comment tags:

```java
//CHECKSTYLE:OFF
private String Violating_STYLEGUIDE_lIterAL = "bar";
//CHECKSTYLE:ON
```

## PMD

PMD is applied with the following [ruleset](https://github.com/hmcts/gradle-java-plugin/blob/master/src/main/resources/pmd-ruleset.xml).

