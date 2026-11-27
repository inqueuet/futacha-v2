import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// The small part Wear OS shares with the phone (Phase 3, docs/v2-phase3-plan.md): the watch protocol types and the
// AI deep link. No Compose, no AI runtime, so :app-wear does not have to pull :shared. Package names are unchanged
// (com.valoser.futacha.shared.watch / .ai) so the serial names and the Data Layer stay the same.
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.valoser.futacha.core.wearable"
    compileSdk { version = release(37) { minorApiLevel = 2 } }
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.junit)
}
