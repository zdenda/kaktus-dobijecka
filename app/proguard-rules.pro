# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Programy\Android\android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontwarn com.google.android.gms.auth.*
-keep class com.google.android.gms.gcm.** { *; }

# Needed by google-api-client to keep generic types and @Key annotations accessed via reflection
-keepclasseswithmembers class * {
  @com.google.api.client.util.Key <fields>;
}

-keepattributes EnclosingMethod

# Needed by Guava
-dontwarn sun.misc.Unsafe

# switch off notes about duplicate class definitions
-dontnote org.apache.http.**
-dontnote android.net.http.*

# Picasso doesn't need OkHttp, it can use HttpURLConnection to download images
-dontwarn com.squareup.okhttp.**
