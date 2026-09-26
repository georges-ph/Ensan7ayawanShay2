import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    // START: FlutterFire Configuration
    id("com.google.gms.google-services")
    // END: FlutterFire Configuration
    id("kotlin-android")
    // The Flutter Gradle Plugin must be applied after the Android and Kotlin Gradle plugins.
    id("dev.flutter.flutter-gradle-plugin")
}

// Release signing credentials live in a local, gitignored key.properties
// (see key.properties.example) - never in this file or in git. Falls back
// to debug signing if that file is missing, so a fresh clone still builds.
val keystorePropertiesFile = rootProject.file("key.properties")
val keystoreProperties = Properties()
val hasReleaseSigning = keystorePropertiesFile.exists()
if (hasReleaseSigning) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

// Auto-select which Firebase project's google-services.json is active,
// based on which build type Gradle was actually asked to build - a
// release build MUST get the production config (google-services-prod.json)
// and debug/profile MUST get dev (google-services-dev.json). This used to
// be a manual copy-the-right-file-before-building step; forgetting it
// means the native side auto-initializes Firebase against one project
// while lib/main.dart's kReleaseMode switch initializes it against the
// other, which crashes the app on launch (duplicate/mismatched default
// FirebaseApp). Doing this at configuration time, before any task runs,
// makes that mistake structurally impossible instead of relying on
// remembering a manual step.
val requestedTasks = gradle.startParameter.taskNames.joinToString(" ").lowercase()
val googleServicesVariant = if (requestedTasks.contains("release")) "prod" else "dev"
val googleServicesSource = file("google-services-$googleServicesVariant.json")
if (googleServicesSource.exists()) {
    googleServicesSource.copyTo(file("google-services.json"), overwrite = true)
    logger.lifecycle("Using google-services.json for: $googleServicesVariant")
}

android {
    namespace = "ga.jundbits.ensan7ayawanshay2"
    compileSdk = flutter.compileSdkVersion
    ndkVersion = flutter.ndkVersion

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    defaultConfig {
        // TODO: Specify your own unique Application ID (https://developer.android.com/studio/build/application-id.html).
        applicationId = "ga.jundbits.ensan7ayawanshay2"
        // You can update the following values to match your application needs.
        // For more information, see: https://flutter.dev/to/review-gradle-config.
        minSdk = flutter.minSdkVersion
        targetSdk = flutter.targetSdkVersion
        versionCode = flutter.versionCode
        versionName = flutter.versionName
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
            }
        }
    }

    buildTypes {
        release {
            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("release")
            } else {
                // Falls back to debug signing so the build doesn't break
                // when key.properties hasn't been created yet.
                signingConfigs.getByName("debug")
            }
        }
    }
}

flutter {
    source = "../.."
}
