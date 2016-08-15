# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\zhiyong\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
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

-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-useuniqueclassmembernames
-keepattributes SourceFile,LineNumberTable
-allowaccessmodification



# Keep the pojos used by GSON or Jackson
-keep class orbital.com.foodsearch.models.** { *; }

# Keep GSON stuff
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }



# Keep these for GSON and Jackson
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

-keepattributes Signature
-keepattributes Exceptions
-keepattributes JNINamespace
-keepattributes CalledByNative
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# Keep Retrofit
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.** *;
}
-keepclassmembers class * {
    @retrofit.** *;
}

# Keep Picasso
-keep class com.squareup.picasso.** { *; }
-keepclasseswithmembers class * {
    @com.squareup.picasso.** *;
}
-keepclassmembers class * {
    @com.squareup.picasso.** *;
}

-adaptresourcefilenames    **.properties,**.gif,**.jpg
-adaptresourcefilecontents **.properties,META-INF/MANIFEST.MF



-keep class android.content.Intent
-keep class com.sec.android.gallery3d.app.** { *; }
-keep class org.apache.http.** { *; }
-keep class okio.** { *; }
-keep class retrofit2.** { *; }
-keep class com.squareup.** { *; }
-keep class firebase.** { *; }
-keep class com.android.** { *; }
-keep class jp.wasabeef.** { *; }
-keep class com.mikhaellopez.** { *; }
-keep class com.theartofdev.** { *; }
-keep class me.everything.** { *; }
-keep class com.github.** { *; }
-keep class com.aurelhubert.** { *; }
-keep class me.zhanghai.** { *; }
-dontwarn okio.**
-dontwarn com.squareup.picasso.**
-dontwarn retrofit2.**
