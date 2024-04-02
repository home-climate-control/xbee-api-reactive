plugins {
    application
}

application {
    applicationName = "xbee-node-discover"
    mainClass.set("com.homeclimatecontrol.xbee.zigbee.NodeDiscoverApp")
}

dependencies {
    implementation(project(":xbee-api"))
    implementation(libs.reactor.core)
}
