import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

object ProjectConfig {
    const val APP_ID = "io.track.habit"
    const val APP_MIN_SDK = 24
    const val APP_COMPILE_SDK = 36
    const val APP_TARGET_SDK = 35

    private const val APP_VERSION_MAJOR = 1
    private const val APP_VERSION_MINOR = 0
    private const val APP_VERSION_PATCH = 0
    private const val APP_VERSION_BUILD = 0

    const val APP_VERSION_NAME = "$APP_VERSION_MAJOR.$APP_VERSION_MINOR.$APP_VERSION_PATCH"
    const val APP_VERSION_CODE =
        APP_VERSION_MAJOR * 10000 + APP_VERSION_MINOR * 1000 + APP_VERSION_PATCH * 100 + APP_VERSION_BUILD
}

android {
    namespace = ProjectConfig.APP_ID
    compileSdk = ProjectConfig.APP_COMPILE_SDK

    defaultConfig {
        applicationId = ProjectConfig.APP_ID
        minSdk = ProjectConfig.APP_MIN_SDK
        targetSdk = ProjectConfig.APP_TARGET_SDK
        versionCode = ProjectConfig.APP_VERSION_CODE
        versionName = ProjectConfig.APP_VERSION_NAME

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_19
        targetCompatibility = JavaVersion.VERSION_19
    }

    kotlinOptions {
        jvmTarget = "19"
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            merges += "META-INF/LICENSE.md"
            merges += "META-INF/LICENSE-notice.md"
        }
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.text.google.fonts)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)

    implementation(libs.datastore.preferences)

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.core)

    implementation(libs.coroutines)
    implementation(libs.gson)

    ksp(libs.room.compiler)
    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
    testImplementation(libs.room.testing)

    implementation(libs.hilt.navigation)
    implementation(libs.lifecycle.compose.runtime)
    implementation(libs.lifecycle.compose.viewmodel)

    ksp(libs.hilt.compiler)
    implementation(libs.hilt.android)

    detektPlugins(libs.detekt.twitter.compose.rules)
    detektPlugins(libs.detekt.formatting)

    testImplementation(libs.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.strikt)
    testImplementation(libs.turbine)

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.ext.junit.ktx)
    // androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.coroutines.test)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.mockk)
    androidTestImplementation(libs.strikt)
    androidTestImplementation(libs.turbine)
}

tasks.withType<Detekt> {
    parallel = true
    config.setFrom(files("${rootProject.rootDir}/config/detekt/detekt.yml"))
    include("**/*.kt")
    include("**/*.kts")
    exclude("**/resources/**")
    exclude("**/build/**")
    reports {
        txt {
            required.set(true)
        }
        sarif {
            required.set(false)
        }
        xml {
            required.set(false)
        }
        md {
            required.set(false)
        }
        html {
            required.set(false)
        }
    }
}
