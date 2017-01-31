# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Android\sdk/tools/proguard/proguard-android.txt
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


# QQ ここの設定ワケワカメ
#-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }

#-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

#-dontwarn org.apache.**
#-dontwarn sun.misc.Unsafe
#-keep class sun.misc.Unsafe

-keep class com.google.**
-dontwarn com.google.**

#-keep sun.misc.Unsafe


#-dontwarn org.w3c.dom.bootstrap.DOMImplementationRegistry

-keep class com.google.gson.**
