# HMCTS Gradle Java Plugin ![Java CI](https://github.com/hmcts/gradle-java-plugin/workflows/Java%20CI/badge.svg) ![GitHub tag (latest SemVer)](https://img.shields.io/github/v/tag/hmcts/gradle-java-plugin?label=release)


This plugin assists applying HMCTS standards to Java projects.

## Usage

Apply the plugin in your plugins block in `build.gradle`:

```groovy
plugins {
    id 'uk.gov.hmcts.java' version '[See badge for latest version]'
}
```

## Checkstyle

Checkstyle is automatically applied and configured to use the HMCTS styleguide.

If you need access to the checkstyle.xml file, eg. for configuring an IDE, it is extracted into a
 .config folder in your project root.

## Exemptions

Checkstyle can be selectively disabled for a block using comment tags:

```java
    //CHECKSTYLE:OFF
    private String Violating_STYLEGUIDE_lIterAL = "bar";
    //CHECKSTYLE:ON
```

