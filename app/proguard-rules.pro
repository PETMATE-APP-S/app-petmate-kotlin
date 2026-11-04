# Reglas de ProGuard para el artefacto de release.

# Modelos serializados con kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class mx.petmate.app.data.remote.dto.** {
    *** Companion;
}
-keepclasseswithmembers class mx.petmate.app.data.remote.dto.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit
-keepattributes Signature, Exceptions
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
