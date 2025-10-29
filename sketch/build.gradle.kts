import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.maven.publish)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

android {
    namespace = "com.mitteloupe.sketch"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

ktlint {
    android = true
}

ext {
    set("PUBLISH_ARTIFACT_ID", "sketch")
    set("PUBLISH_VERSION", "0.1.0")
}

mavenPublishing {
    coordinates(
        groupId = "com.mitteloupe.sketch",
        artifactId = "sketch",
        version = libs.versions.sketch.get()
    )

    pom {
        name.set("Sketch")
        description.set(
            "A Jetpack Compose library providing sketch-style versions of common Material 3 UI components."
        )
        inceptionYear.set("2025")
        url.set("https://github.com/EranBoudjnah/sketch/")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://mit-license.org")
                distribution.set("https://mit-license.org")
            }
        }
        developers {
            developer {
                id.set("EranB")
                name.set("Eran Boudjnah")
                url.set("https://github.com/EranBoudjnah/")
            }
        }
        scm {
            url.set("https://github.com/EranBoudjnah/sketch/")
            connection.set("scm:git:git://github.com/EranBoudjnah/sketch.git")
            developerConnection.set("scm:git:ssh://git@github.com/EranBoudjnah/sketch.git")
        }
    }
}

signing {
    useGpgCmd()
}
