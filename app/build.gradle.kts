import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.UUID

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.bugsnagAndroid)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "io.github.turtlepaw.blueskyredirect"
    compileSdk = rootProject.extra["compile.sdk"].toString().toInt()

    defaultConfig {
        applicationId = "io.github.turtlepaw.blueskyredirect"
        minSdk = rootProject.extra["min.sdk"].toString().toInt()
        targetSdk = rootProject.extra["target.sdk"].toString().toInt()
        versionCode = rootProject.extra["version.code"].toString().toInt()
        versionName = rootProject.extra["version.name"].toString()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        extensions.getByType(BasePluginExtension::class.java).archivesName.set("BlueskyRedirect_${versionName}")
        manifestPlaceholders["build_uuid"] =
            UUID.nameUUIDFromBytes("BlueskyRedirect_${versionCode}".toByteArray()).toString()
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            isMinifyEnabled = false
        }
    }
    val javaVersion = JavaVersion.toVersion(rootProject.extra["java.version"].toString().toInt())
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(javaVersion.majorVersion))
        }
    }
    buildFeatures {
        compose = true
        aidl = true
        buildConfig = true
    }
    packaging {
        resources.excludes.add("META-INF/versions/9/previous-compilation-data.bin")
    }
}

dependencies {
    implementation(project(":shared"))
}