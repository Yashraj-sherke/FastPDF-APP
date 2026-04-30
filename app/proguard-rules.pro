# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# FastPDF ProGuard Rules — Phase 10: Release Ready
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

# ━━━ Jetpack Compose ━━━
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# ━━━ Navigation Compose ━━━
-keepclassmembers class * extends androidx.navigation.NavArgs {
    *;
}
-keep class com.fastpdf.navigation.** { *; }

# ━━━ Room Database ━━━
-keep class com.fastpdf.data.db.** { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# ━━━ iText 7 (PDF Processing) ━━━
-dontwarn com.itextpdf.**
-keep class com.itextpdf.** { *; }
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# ━━━ Google ML Kit ━━━
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# ━━━ Google Generative AI (Gemini) ━━━
-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**

# ━━━ Coil (Image Loading) ━━━
-dontwarn coil.**
-keep class coil.** { *; }

# ━━━ DataStore ━━━
-keep class androidx.datastore.** { *; }

# ━━━ Coroutines ━━━
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

# ━━━ Kotlin Serialization (if used) ━━━
-dontwarn kotlinx.serialization.**
-keepattributes *Annotation*, InnerClasses

# ━━━ General Android ━━━
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepattributes Signature
-keepattributes Exceptions

# ━━━ FastPDF Domain Models ━━━
-keep class com.fastpdf.domain.model.** { *; }
-keep enum com.fastpdf.domain.model.** { *; }
