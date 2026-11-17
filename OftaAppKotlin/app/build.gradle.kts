import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    jacoco
}

android {
    namespace = "com.equipo7.oftaapp"
    compileSdk {
        version = release(37)
    }

    val fhirBaseUrl = providers.gradleProperty("FHIR_BASE_URL")
        .orElse(providers.environmentVariable("FHIR_BASE_URL"))
        .orElse("http://10.0.2.2:8080/fhir/")

    defaultConfig {
        applicationId = "com.equipo7.oftaapp"
        minSdk = 26
        targetSdk = 37
        versionCode = 2
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "FHIR_BASE_URL", "\"${fhirBaseUrl.get()}\"")
    }

    // Firma release: credenciales leídas de keystore.properties (IGNORADO por Git).
    // El .jks se conserva fuera del repositorio; ver keystore.properties.example.
    signingConfigs {
        create("release") {
            val propiedades = Properties()
            val archivo = rootProject.file("keystore.properties")
            if (archivo.exists()) {
                FileInputStream(archivo).use { propiedades.load(it) }
                storeFile = rootProject.file(propiedades.getProperty("storeFile"))
                storePassword = propiedades.getProperty("storePassword")
                keyAlias = propiedades.getProperty("keyAlias")
                keyPassword = propiedades.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            // Reporte de cobertura exigido por la rúbrica EP4/EP5
            enableUnitTestCoverage = true
        }
        release {
            optimization {
                enable = false
            }
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        // Permite ejecutar en JVM pruebas que tocan utilidades Android (p. ej. android.util.Log),
        // cuyos stubs lanzan "Stub!" si esta opción está desactivada.
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    // API/FHIR: Retrofit para HTTP desde Android; backend HAPI FHIR expone recursos R4
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)
    // Almacenamiento cifrado de preferencias/sesión respaldado por Android Keystore
    implementation(libs.androidx.security.crypto)
    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
