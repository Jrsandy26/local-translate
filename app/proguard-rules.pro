# ProGuard & R8 optimization rules for RivaTranslate

# 1. Jetpack Compose and Android Architecture Components
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel { *; }

# 2. Room Database and DAOs
# Keep the database definition and the generated implementation
-keep class * extends androidx.room.RoomDatabase
-keep class * implements com.rivatranslate.data.db.TranslationDao
-keep class com.rivatranslate.data.db.** { *; }
-keep class com.rivatranslate.model.** { *; }
-dontwarn androidx.room.paging.**

# 3. Kotlinx Serialization
# Ensure @Serializable annotated classes can be serialized/deserialized properly
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    *** Companion;
}
-keep class * {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keep class **$$serializer { *; }
-keepclassmembers class * {
    *** write$Self(...);
}

# 4. Google ML Kit and Translation Models
# ML Kit relies on native code, reflection, and GMS services
-keep class com.google.mlkit.** { *; }
-keep interface com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_translate.** { *; }
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.**

# 5. Services and Providers
-keep class com.rivatranslate.service.LiveTranslationService { *; }
-keep class androidx.core.content.FileProvider { *; }

# 6. General optimization settings for real-world devices
-repackageclasses ''
-allowaccessmodification

# 7. Additions for stability with R8
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
