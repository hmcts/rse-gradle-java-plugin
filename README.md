# Gradle Java Plugin

This plugin applies HMCTS standards for Java projects.

## Usage

Apply the plugin
```groovy
plugins {
    id 'uk.gov.hmcts.java' version '[See badge for latest version]'
}
```

## Checkstyle

Checkstyle is automatically applied and configured to use the HMCTS styleguide.

If you need access to the checkstyle.xml file, eg. for configuring an IDE, it is extracted into a
 .config folder in your project roo in your project root.
