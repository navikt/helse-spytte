plugins {
    kotlin("jvm") version "1.6.10"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

val rapidsAndRiversCliVersion = "1.be82170"

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.github.navikt:rapids-and-rivers-cli:$rapidsAndRiversCliVersion")
}
