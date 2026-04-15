# FastPDF ProGuard Rules

# Keep Compose
-dontwarn androidx.compose.**

# Keep Navigation arguments
-keepclassmembers class * extends androidx.navigation.NavArgs {
    *;
}

# Keep data classes used for navigation
-keep class com.fastpdf.navigation.** { *; }
