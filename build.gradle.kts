plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.3.72"
    id("org.mikeneck.graalvm-native-image") version "v0.5.0"

}

group = "org.zarkone"
version = "1.0-SNAPSHOT"

application {
    mainClassName = "org.zarkone.faillogs.App"
}
repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.github.kittinunf.fuel:fuel:2.2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")

    testImplementation("junit:junit:4.12")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
}

nativeImage {
    setGraalVmHome(System.getProperty("java.home"))
    setMainClass("org.zarkone.faillogs.App")
    setExecutableName("faillogs")
    setOutputDirectory("$buildDir/executable")
    arguments(
            "--no-fallback",
            "-H:EnableURLProtocols=https",
            "--enable-all-security-services",
            "--report-unsupported-elements-at-runtime"
    )
}