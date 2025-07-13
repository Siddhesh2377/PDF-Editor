plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.chaquo.python") version "16.1.0"
}

android {
    namespace = "com.example.pdfeditor"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.pdfeditor"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        ndk {
            abiFilters += listOf("arm64-v8a")
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    flavorDimensions += "pyVersion"
    productFlavors {
        create("py310") { dimension = "pyVersion" }
        create("py311") { dimension = "pyVersion" }
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

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

chaquopy {
    productFlavors {
        getByName("py310") { version = "3.10" }
        getByName("py311") { version = "3.11" }
    }

    sourceSets.getByName("main") {
        setSrcDirs(listOf("src/main/python"))
    }

    defaultConfig {
        pip {
            install("fpdf")
        }
    }
}

dependencies {
    implementation("com.github.bhuvaneshw.pdfviewer:compose:1.1.1")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("com.github.bhuvaneshw.pdfviewer:compose-ui:1.1.1")

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}