plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    // Add the dependency for the Google services Gradle plugin firebase. para probar la hu1 cm
    id("com.google.gms.google-services") version "4.4.4" apply false

}
