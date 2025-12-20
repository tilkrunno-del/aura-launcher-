plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.aura.launcher"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.aura.launcher"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
}
