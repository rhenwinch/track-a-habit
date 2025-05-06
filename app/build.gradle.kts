import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ksp)
}

object ProjectConfig {
    const val APP_ID = "io.track.habit"

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
    compileSdk = 35

    defaultConfig {
        applicationId = ProjectConfig.APP_ID
        minSdk = 24
        targetSdk = 35
        versionCode = ProjectConfig.APP_VERSION_CODE
        versionName = ProjectConfig.APP_VERSION_NAME

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_19
        targetCompatibility = JavaVersion.VERSION_19
    }

    kotlinOptions {
        jvmTarget = "19"
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
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.coroutines)

    implementation(libs.hilt.navigation)
    implementation(libs.lifecycle.compose.runtime)
    implementation(libs.lifecycle.compose.viewmodel)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    detektPlugins(libs.detekt.twitter.compose.rules)
    detektPlugins(libs.detekt.formatting)

    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.junit)

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.ext.junit.ktx)
    // androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.coroutines.test)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.mockk)
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
