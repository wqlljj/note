# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/zoey/Library/Android/sdk/tools/proguard/proguard-android.txt
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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class com.cloudminds.hc.hariservice.HariServiceClient{*;}
-keep public class com.getui.logful.**

-dontwarn org.apache.log4j.**
-keep class org.apache.log4j.** { *;}

-dontwarn com.tendcloud.tenddata.**
-keep class com.tendcloud.tenddata.** { *;}
-keeppackagenames com.cloudminds.hc.hariservice.**
-keeppackagenames org.webrtc.**
-keep class com.cloudminds.hc.hariservice.call.CallEngine{*;}
-keep class com.cloudminds.hc.hariservice.utils.ThreadPoolUtils{*;}
-keep class org.webrtc.UsbCameraEnumerator{*;}
-keep class com.cloudminds.hc.hariservice.webrtc.LooperExecutor{*;}
-keep class com.cloudminds.hc.hariservice.utils.HariUtils{*;}
-keep class com.cloudminds.hc.hariservice.utils.PreferenceUtils{*;}
-keep class com.cloudminds.hc.hariservice.call.listener.**{*;}
-keep class com.cloudminds.hc.hariservice.webrtc.AppRTCAudioManager{*;}
-keep class com.cloudminds.hc.hariservice.utils.BaseConstants{*;}
-keep class com.cloudminds.hc.hariservice.command.CommandEngine{*;}
-keep class com.cloudminds.hc.hariservice.command.listener.CmdEventListener{*;}
-keep class com.cloudminds.hc.hariservice.HariServiceClient$InitServiceCallback{*;}
-keep class com.cloudminds.hc.hariservice.call.CallEvent{*;}
-keep  class com.cloudminds.hc.hariservice.service.HariServiceConnector{*;}
-keepclassmembers class com.cloudminds.hc.hariservice.service.HariService{
    public boolean isInit;
}
-keep public enum com.cloudminds.hc.hariservice.call.CallEngine$Callee  {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepattributes *Annotation*
-keepclassmembers class ** {
    @de.greenrobot.event.Subscribe <methods>;
}
-keep enum de.greenrobot.event.ThreadMode { *; }
# Only required if you use AsyncExecutor
-keepclassmembers class * extends de.greenrobot.event.util.ThrowableFailureEvent {
    <init>(Java.lang.Throwable);
}

-keepclassmembers class ** {
    public void onEvent*(**);
}

