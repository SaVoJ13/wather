# ProGuard / R8 Rules for Weather App

# Kotlin & Coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }

# Jetpack Compose & Glance
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keep class androidx.glance.** { *; }
-dontwarn androidx.glance.**

# App Specific Data Models (JSON caching & reflection safety)
-keep class com.weather.app.domain.model.** { *; }
-keep class com.weather.app.data.remote.dto.** { *; }
-keep class com.weather.app.widget.** { *; }

# AndroidX Lifecycle & ViewModel
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
