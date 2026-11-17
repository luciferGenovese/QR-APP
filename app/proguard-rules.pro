# Add project specific ProGuard rules here.
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep ZXing core barcode classes and methods
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# Keep MLKit Barcode Vision classes
-keep class com.google.mlkit.vision.** { *; }
-dontwarn com.google.mlkit.vision.**
-keep class com.google.android.gms.vision.** { *; }
-dontwarn com.google.android.gms.vision.**
-keep class com.google.android.gms.tasks.** { *; }
-dontwarn com.google.android.gms.tasks.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Keep CameraX classes
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# Keep Room database and DAO classes
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase {
    public <init>();
    *;
}
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.**

# Keep data models and generated Room implementation
-keep class com.example.data.** { *; }
-keepclassmembers class com.example.data.** { *; }
-keep class com.example.data.AppDatabase_Impl {
    public <init>();
    *;
}

# Keep ViewModels and UI classes
-keep class com.example.ui.** { *; }
-keepclassmembers class com.example.ui.** { *; }
-keep class com.example.utils.** { *; }
-keepclassmembers class com.example.utils.** { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keepclassmembers class * extends androidx.lifecycle.ViewModelProvider$Factory {
    <init>(...);
}

# Compose & Navigation
-keep class androidx.compose.** { *; }
-keep class androidx.navigation.** { *; }

# Coil
-keep class coil.** { *; }
-dontwarn coil.**

# Moshi & OkHttp
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonQualifier interface * { *; }
-keep class * extends com.squareup.moshi.JsonAdapter { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**


