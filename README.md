# HMCTS Gradle Java Plugin ![Java CI](https://github.com/hmcts/gradle-java-plugin/workflows/Java%20CI/badge.svg)


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
