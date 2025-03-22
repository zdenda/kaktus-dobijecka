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

-dontwarn org.apache.commons.codec.binary.**

-keepattributes EnclosingMethod

# Needed by Guava
-dontwarn sun.misc.Unsafe
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.concurrent.LazyInit
-dontwarn com.google.errorprone.annotations.ForOverride
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn afu.org.checkerframework.**
-dontwarn org.checkerframework.**
-dontwarn com.google.errorprone.**
-dontwarn java.lang.ClassValue

# switch off notes about duplicate class definitions
-dontnote org.apache.http.**
-dontnote android.net.http.*

# Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
