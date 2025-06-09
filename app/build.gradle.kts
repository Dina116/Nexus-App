plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")

}

android {
    namespace = "com.training.graduation"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.training.graduation"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
    buildFeatures {
        compose = true
    }
    packagingOptions {
        resources {
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
            excludes += "META-INF/native-image/org.mongodb/bson/native-image.properties"

        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
//    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.benchmark.macro)
    implementation(libs.material)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.firebase.common.ktx)
    implementation(libs.firebase.storage.ktx)
    //implementation(libs.androidx.navigationevent.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation( libs.androidx.animation)

    implementation (libs.androidx.material.icons.extended)

    implementation (libs.androidx.navigation.compose.v260)

    implementation(libs.animated.navigation.bar)

    implementation(libs.coil.compose)
    implementation(libs.coil)

//    implementation (libs.sdk)

    implementation("org.jitsi.react:jitsi-meet-sdk:+") {
        isTransitive = true
    }
    implementation ("org.jitsi.react:jitsi-meet-sdk:11.1.4")
    implementation ("com.google.firebase:firebase-messaging")


    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")


        // Realm Kotlin SDK
    implementation("io.realm.kotlin:library-base:1.16.0")

        // Coroutine support (مطلوب لأن Realm يعمل مع الكوروتين)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("io.realm.kotlin:library-sync:1.16.0")

    coreLibraryDesugaring ("com.android.tools:desugar_jdk_libs:2.1.4")


    implementation(libs.mongodb.driver.sync)


    implementation(libs.bcrypt)

    //firebase
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-messaging")
    //implementation ("com.google.firebase:firebase-firestore-ktx")



    //fire store
    implementation("com.google.firebase:firebase-firestore")


//    // Microsoft Identity Client

    implementation("com.microsoft.identity.client:msal:1.4.0")

    implementation ("com.github.bumptech.glide:glide:4.16.0")

    implementation ("com.sun.mail:android-mail:1.6.6")
    implementation ("com.sun.mail:android-activation:1.6.6")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")



    // CameraX
    implementation ("androidx.camera:camera-core:1.1.0")
    implementation("androidx.camera:camera-camera2:1.2.3")
    implementation("androidx.camera:camera-lifecycle:1.2.3")
    implementation("androidx.camera:camera-view:1.2.3")
    implementation ("androidx.lifecycle:lifecycle-process:2.6.1")
    implementation ("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")


// iText PDF
    implementation ("com.itextpdf:itextpdf:5.5.13.3")

    implementation ("org.tensorflow:tensorflow-lite:2.7.0")


    implementation( "androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")


}
apply(plugin = "com.google.gms.google-services")
