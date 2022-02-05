plugins {
    kotlin("jvm") version "1.6.10"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

val rapidsAndRiversCliVersion = "1.be82170"

dependencies {
    implementation("com.github.navikt:rapids-and-rivers-cli:$rapidsAndRiversCliVersion")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    named<Jar>("jar") {
        archiveFileName.set("app.jar")

        manifest {
            attributes["Main-Class"] = "no.nav.helse.spytte.AppKt"
            attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(separator = " ") {
                it.name
            }
        }

        doLast {
            configurations.runtimeClasspath.get().forEach {
                val file = File("$buildDir/libs/${it.name}")
                if (!file.exists())
                    it.copyTo(file)
            }
        }
    }

    withType<Wrapper> {
        gradleVersion = "7.3.3"
    }
}