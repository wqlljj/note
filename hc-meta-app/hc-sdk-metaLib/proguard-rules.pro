# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Android\sdk/tools/proguard/proguard-android.txt
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
-keep class com.cloudminds.hc.metalib.USBUtils{*;}
-keep class com.cloudminds.hc.metalib.USBUtils$MetaHotSwapListener{*;}
-keep class com.cloudminds.hc.metalib.UpdaterApplication$Installer{*;}
-keep class com.cloudminds.hc.metalib.HCMetaUtils{*;}
-keep class com.cloudminds.hc.metalib.UpdaterApplication{*;}
-keep class com.cloudminds.hc.metalib.utils.ToastUtil{*;}
-keep class com.cloudminds.hc.metalib.bean.**{*;}
-keep class com.cloudminds.hc.metalib.manager.**{*;}
-keep class com.cloudminds.hc.metalib.broadcast.**{*;}
-keep class com.cloudminds.hc.metalib.LooperExecutor{*;}
-keeppackagenames com.cloudminds.hc.metalib.*

