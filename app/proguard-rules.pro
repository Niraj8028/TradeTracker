# =====================================================================
# TradeTrack — R8 / ProGuard keep rules for the minified release build
# =====================================================================

# ---- Crashlytics: keep readable stack traces ----
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,AnnotationDefault

# ---------------------------------------------------------------------
# Firebase Firestore
# DocumentSnapshot.toObject(Foo::class.java) populates model objects by
# reflecting on field names — R8 renaming those fields makes toObject()
# return objects full of nulls/defaults. Keep every class we deserialize.
# ---------------------------------------------------------------------
-keep class com.wallstreet.data.model.** { *; }
-keepclassmembers class com.wallstreet.data.model.** {
    <init>();
    <fields>;
}

# Firestore field annotations must survive on those models
-keepclassmembers class * {
    @com.google.firebase.firestore.DocumentId <fields>;
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.ServerTimestamp <fields>;
    @com.google.firebase.firestore.Exclude <fields>;
}
-keepclasseswithmembers class * {
    @com.google.firebase.firestore.DocumentId <fields>;
}

# Enums stored as Firestore fields / serialized by name
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    **[] $VALUES;
    public *;
}
-keep enum com.wallstreet.domain.model.TradeType { *; }
-keep enum com.wallstreet.domain.model.TrendDirection { *; }

# ---------------------------------------------------------------------
# Room
# ---------------------------------------------------------------------
-keep class com.wallstreet.data.local.entity.** { *; }
-keep enum com.wallstreet.data.local.entity.SyncStatus { *; }

# ---------------------------------------------------------------------
# kotlinx.serialization — Navigation 3 @Serializable route keys
# (navigation/Routes.kt: AppRoute + nested objects/classes)
# ---------------------------------------------------------------------
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.wallstreet.**$$serializer { *; }
-keepclassmembers class com.wallstreet.** {
    *** Companion;
}
-keepclasseswithmembers class com.wallstreet.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---------------------------------------------------------------------
# Legacy Google Sign-In (GMS)
# ---------------------------------------------------------------------
-keep class com.google.android.gms.auth.api.signin.** { *; }

# ---------------------------------------------------------------------
# Misc noise from transitive deps
# ---------------------------------------------------------------------
-dontwarn org.slf4j.**
-dontwarn javax.annotation.**
