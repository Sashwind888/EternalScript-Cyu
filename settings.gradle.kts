pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven {
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
    }
    plugins {
        id("org.jetbrains.kotlin.jvm").version("2.3.0")
        id("org.jetbrains.kotlin.plugin.serialization").version("2.3.0")
        id("io.papermc.paperweight.userdev").version("2.0.0-beta.19")
        id("xyz.jpenilla.run-paper").version("3.0.2")
        id("xyz.jpenilla.resource-factory-paper-convention").version("1.3.1")
    }
}

rootProject.name = "EternalScript-Cyu"
