plugins {
    kotlin("jvm") version "2.4.20-RC3"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.23"
    id("com.gradleup.shadow") version "9.6.1"
}

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("26.2.build.+")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

kotlin.jvmToolchain(25)

tasks {
    build {
        dependsOn(shadowJar)
    }

    processResources {
        val props = mapOf("version" to version, "description" to project.description)

        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}
