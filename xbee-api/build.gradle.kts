plugins {
   `java-library`
}

dependencies {

    api(libs.log4j.api)

    implementation(libs.reactor.core)
    implementation(libs.rxtx)

    testImplementation(libs.assertj.core)
    testImplementation(libs.junit5.api)
    testImplementation(libs.junit5.params)
    testImplementation(libs.mockito)
    testImplementation(libs.reactor.test)
    testImplementation(libs.reactor.tools)

    testRuntimeOnly(rootProject.libs.junit5.engine)
}
