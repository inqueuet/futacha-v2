import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// An ordinary Android library (Phase 2, docs/v2-phase2-plan.md): sources in src/main, tests in src/test.
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.valoser.futacha.shared"
    compileSdk { version = release(37) { minorApiLevel = 2 } }
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    sourceSets {
        getByName("test") {
            // Identical small synthetic videos for the common and Android codec tests.
            kotlin.directories += "src/videoTestFixtures/kotlin"
        }
    }
    testOptions {
        unitTests {
            // Android's stub Log methods otherwise throw instead of returning defaults.
            isReturnDefaultValues = true
            // Robolectric reads the merged resources.
            isIncludeAndroidResources = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    // The watch protocol and the AI deep link (Phase 3); the phone code keeps using them as before.
    api(project(":core-wearable"))
    // Compose comes from the AndroidX BOM; the JetBrains Compose artifacts this replaced resolved to the same
    // AndroidX versions (build/phase2-c dependency comparison).
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.runtime.saveable)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.compose.ui.geometry)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugRuntimeOnly(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.coil3.compose)
    implementation(libs.coil3.core)
    implementation(libs.coil3.compose.core)
    implementation(libs.coil3.network.core)
    implementation(libs.okio)
    implementation(libs.coil3.network.ktor)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.http)
    implementation(libs.ktor.io)
    implementation(libs.ktor.utils)
    implementation(libs.onnxruntime.android)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.core.ktx)
    // Align Coil's transitive EXIF decoder with instrumentation.
    implementation(libs.androidx.exifinterface)
    implementation(libs.okhttp)
    implementation(libs.guava)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.androidx.activity.compose)
    // Fix delayed Android back-animation callbacks after input removal/disposal.
    // Navigation Event's atomic-group constraints also align its Compose artifact.
    implementation(libs.androidx.navigationevent)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.effect)
    implementation(libs.androidx.media3.transformer)
    implementation(libs.androidx.media3.database)
    implementation(libs.androidx.media3.datasource)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.documentfile)
    implementation(libs.coil3.video)
    implementation(libs.coil3.gif)
    // Coil/ImageDecoder does not decode APNG. The compatibility mode
    // must keep the sample client's APNG support on API 26+.
    implementation(libs.penfeizhou.animation.apng)
    // Android 26/27 do not have ImageDecoder's animated-WebP
    // support; the reference client decodes those frames too.
    implementation(libs.penfeizhou.animation.awebp)
    implementation(libs.penfeizhou.animation.core)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.common)
    implementation(libs.firebase.performance)
    implementation(libs.firebase.crashlytics)
    implementation(libs.mlkit.genai.summarization)
    implementation(libs.mlkit.genai.prompt)
    implementation(libs.mlkit.common)
    implementation(libs.mlkit.genai.common)
    implementation(libs.play.services.tasks)
    implementation(libs.google.play.billing)

    constraints {
        // Keep what the JetBrains Compose artifacts resolved to before the switch: Coil still pulls some of them in
        // (they redirect to AndroidX on Android), and their material3 had raised graphics-shapes.
        val cmp = libs.versions.composeMultiplatform.get()
        listOf(
            "runtime:runtime", "runtime:runtime-saveable", "ui:ui", "ui:ui-geometry", "ui:ui-graphics", "ui:ui-text",
            "ui:ui-unit", "ui:ui-util", "animation:animation", "animation:animation-core", "foundation:foundation",
            "foundation:foundation-layout"
        ).forEach { implementation("org.jetbrains.compose.$it:$cmp") }
        implementation("androidx.graphics:graphics-shapes:1.1.0")
    }

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.test.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${libs.versions.kotlinxCoroutines.get()}")
    testImplementation(libs.kotlinx.io.core)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.junit)
    // Compose UI tests on Robolectric.
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.compose.ui.test.manifest)
}

val validateAiActionCatalog by tasks.registering {
    val commonCommandFile = rootProject.layout.projectDirectory.file("core-wearable/src/main/kotlin/ai/FutachaAiCommand.kt")
    val androidFunctionsFile = rootProject.layout.projectDirectory.file("app-android/src/main/assets/futacha_app_functions.xml")
    val androidAppFunctionServiceFile = rootProject.layout.projectDirectory.file("app-android/src/main/java/com/valoser/futacha/FutachaAppFunctionService.kt")
    val androidManifestFile = rootProject.layout.projectDirectory.file("app-android/src/main/AndroidManifest.xml")
    val androidHelpFile = rootProject.layout.projectDirectory.file("docs/help/help-android.html")

    inputs.file(commonCommandFile)
    inputs.file(androidFunctionsFile)
    inputs.file(androidAppFunctionServiceFile)
    inputs.file(androidManifestFile)
    if (androidHelpFile.asFile.isFile) inputs.file(androidHelpFile)

    doLast {
        fun parseCommonIds(): List<String> {
            return Regex("""^\s*\w+\("([^"]+)",""", RegexOption.MULTILINE)
                .findAll(commonCommandFile.asFile.readText())
                .map { it.groupValues[1] }
                .toList()
        }

        fun parseCommonConfirmIds(): List<String> {
            return Regex("""^\s*\w+\("([^"]+)",\s*"[^"]+",\s*FutachaAiCommandRisk\.Confirm\)""", RegexOption.MULTILINE)
                .findAll(commonCommandFile.asFile.readText())
                .map { it.groupValues[1] }
                .toList()
        }

        fun parseAndroidIds(): List<String> {
            return Regex("""<id>([^<]+)</id>""")
                .findAll(androidFunctionsFile.asFile.readText())
                .map { it.groupValues[1] }
                .toList()
        }

        fun parseAndroidFunctionBlocks(): List<String> {
            return Regex("""<appfunction>(.*?)</appfunction>""", RegexOption.DOT_MATCHES_ALL)
                .findAll(androidFunctionsFile.asFile.readText())
                .map { it.groupValues[1] }
                .toList()
        }

        fun requireAndroidFunctionSchemaNamesMatchIds() {
            parseAndroidFunctionBlocks().forEachIndexed { index, block ->
                val id = Regex("""<id>([^<]+)</id>""")
                    .find(block)
                    ?.groupValues
                    ?.getOrNull(1)
                    ?: error("Missing Android AppFunction id at index $index")
                val schemaName = Regex("""<schemaName>([^<]+)</schemaName>""")
                    .find(block)
                    ?.groupValues
                    ?.getOrNull(1)
                    ?: error("Missing Android AppFunction schemaName for $id")
                require(schemaName == id) {
                    "Android AppFunction schemaName must match id for $id, found $schemaName"
                }
            }
        }

        fun requireAndroidAppFunctionServiceDoesNotEchoDeepLinks() {
            val source = androidAppFunctionServiceFile.asFile.readText()
            require("FutachaAiCommandBridge.enqueue" in source) {
                "Android AppFunctionService must enqueue AI commands through FutachaAiCommandBridge"
            }
            require("""setPropertyString("deepLink"""" !in source) {
                "Android AppFunctionService must not echo deep links in AppFunction results"
            }
            require("getPropertyLongArray" in source && "getPropertyDoubleArray" in source && "getPropertyBooleanArray" in source) {
                "Android AppFunctionService must accept scalar string, number, and boolean parameters"
            }
        }

        fun requireAndroidManifestDeclaresAiEntrypoints() {
            val source = androidManifestFile.asFile.readText()
            require("""android:name=".FutachaAppFunctionService"""" in source) {
                "Android manifest must declare FutachaAppFunctionService"
            }
            require("""android.permission.BIND_APP_FUNCTION_SERVICE""" in source) {
                "Android AppFunctionService must require BIND_APP_FUNCTION_SERVICE"
            }
            require("""android:name="android.app.appfunctions"""" in source &&
                """android:value="futacha_app_functions.xml"""" in source
            ) {
                "Android manifest must link AppFunction service to futacha_app_functions.xml"
            }
            require("""android.app.appfunctions.AppFunctionService""" in source) {
                "Android AppFunctionService intent action is missing"
            }
            require("""android:scheme="futacha"""" in source && """android:host="ai"""" in source) {
                "Android manifest must declare futacha://ai deep link entrypoint"
            }
        }

        fun requireHelpDoesNotExposeInternalMvpCounts() {
            val forbidden = Regex("""(?:50|10)\s*(?:件|つ|個)""")
            val privateHelpFiles = listOf(androidHelpFile).filter { it.asFile.isFile }
            privateHelpFiles.forEach { file ->
                val match = forbidden.find(file.asFile.readText())
                require(match == null) {
                    "Help must not expose internal AI MVP counts in ${file.asFile.name}: ${match?.value}"
                }
            }
        }

        fun requireSameCatalog(name: String, expected: List<String>, actual: List<String>) {
            require(actual == expected) {
                val missing = expected.toSet() - actual.toSet()
                val extra = actual.toSet() - expected.toSet()
                buildString {
                    append("AI action catalog mismatch in ")
                    append(name)
                    append(". expected=")
                    append(expected.size)
                    append(", actual=")
                    append(actual.size)
                    if (missing.isNotEmpty()) append(", missing=").append(missing.sorted())
                    if (extra.isNotEmpty()) append(", extra=").append(extra.sorted())
                    if (missing.isEmpty() && extra.isEmpty()) {
                        append(", action ids are the same but order differs")
                    }
                }
            }
        }

        fun requireSameSet(name: String, expected: List<String>, actual: List<String>) {
            val missing = expected.toSet() - actual.toSet()
            val extra = actual.toSet() - expected.toSet()
            require(missing.isEmpty() && extra.isEmpty()) {
                buildString {
                    append("AI action catalog mismatch in ")
                    append(name)
                    append(". expected=")
                    append(expected.size)
                    append(", actual=")
                    append(actual.size)
                    if (missing.isNotEmpty()) append(", missing=").append(missing.sorted())
                    if (extra.isNotEmpty()) append(", extra=").append(extra.sorted())
                }
            }
        }

        val commonIds = parseCommonIds()
        require(commonIds.size >= 50) {
            "Expected at least 50 Futacha AI actions in shared/src/main, found ${commonIds.size}"
        }
        requireSameCatalog("Android AppFunctions XML", commonIds, parseAndroidIds())
        requireAndroidFunctionSchemaNamesMatchIds()
        requireAndroidAppFunctionServiceDoesNotEchoDeepLinks()
        requireAndroidManifestDeclaresAiEntrypoints()
        requireHelpDoesNotExposeInternalMvpCounts()
    }
}

tasks.named("check") {
    dependsOn(validateAiActionCatalog)
}

// Compose compiler stability reports, only when asked for:
// ./gradlew :shared:compileAndroidMain -PcomposeReports=true
if (providers.gradleProperty("composeReports").orNull == "true") {
    composeCompiler {
        reportsDestination = layout.buildDirectory.dir("compose_compiler")
        metricsDestination = layout.buildDirectory.dir("compose_compiler")
    }
}
