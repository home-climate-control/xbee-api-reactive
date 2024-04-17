plugins {
    java
    jacoco
    alias(libs.plugins.errorprone)
    alias(libs.plugins.sonarqube)

    alias(libs.plugins.gradle.versions)
    alias(libs.plugins.gradle.dependency.analysis)
    alias(libs.plugins.gradle.doctor)
}

sonarqube {
    properties {
        property("sonar.projectKey", "home-climate-control_xbee-api-reactive")
        property("sonar.organization", "home-climate-control")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

doctor {
    javaHome {
        // Build breaks in IntelliJ IDEA on macOS even if JAVA_HOME is set correctly
        // (it picks up JetBrains JDK instead)
        failOnError.set(false)
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "jacoco")
    apply(plugin = rootProject.libs.plugins.errorprone.get().pluginId)

    group = "com.homeclimatecontrol"
    version = "0.0.2"

    tasks.compileJava {
        options.release = 11
    }

    jacoco {
        toolVersion = rootProject.libs.versions.jacoco.get()
    }

    tasks.test {
        finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test) // tests are required to run before generating the report
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        errorprone(rootProject.libs.errorprone)
    }

    tasks.test {
        useJUnitPlatform()
    }
}
