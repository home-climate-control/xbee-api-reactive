dependencies {
    implementation(project(":xbee-api"))
    testImplementation(libs.junit5.api)
    testRuntimeOnly(rootProject.libs.junit5.engine)
}
