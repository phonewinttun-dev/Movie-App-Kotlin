# Proguard rules for MovieApp

# Preserve Retrofit and Gson models
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Keep DTO models in features
-keep class com.movieapp.features.**.**DTO { *; }

# Retain Retrofit interfaces
-keep interface com.movieapp.network.** { *; }
