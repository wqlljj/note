/*
 *  Copyright 2013 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package org.webrtc;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Surface;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

// Java-side of peerconnection_jni.cc:MediaCodecVideoEncoder.
// This class is an implementation detail of the Java PeerConnection API.
@TargetApi(19)
@SuppressWarnings("deprecation")
public class MediaCodecVideoEncoder {
  // This class is constructed, operated, and destroyed by its C++ incarnation,
  // so the class and its methods have non-public visibility.  The API this
  // class exposes aims to mimic the webrtc::VideoEncoder API as closely as
  // possibly to minimize the amount of translation work necessary.

  private static final String TAG = "MediaCodecVideoEncoder";

  // Tracks webrtc::VideoCodecType.
  public enum VideoCodecType {
    VIDEO_CODEC_VP8,
    VIDEO_CODEC_VP9,
    VIDEO_CODEC_H264
  }

  private static final int MEDIA_CODEC_RELEASE_TIMEOUT_MS = 5000; // Timeout for codec releasing.
  private static final int DEQUEUE_TIMEOUT = 0;  // Non-blocking, no wait.
  private static final int BITRATE_ADJUSTMENT_FPS = 30;
  // Active running encoder instance. Set in initEncode() (called from native code)
  // and reset to null in release() call.
  private static MediaCodecVideoEncoder runningInstance = null;
  private static MediaCodecVideoEncoderErrorCallback errorCallback = null;
  private static int codecErrors = 0;
  // List of disabled codec types - can be set from application.
  private static Set<String> hwEncoderDisabledTypes = new HashSet<String>();

  private Thread mediaCodecThread;
  private MediaCodec mediaCodec;
  private ByteBuffer[] outputBuffers;
  private EglBase14 eglBase;
  private int width;
  private int height;
  private Surface inputSurface;
  private GlRectDrawer drawer;

  private static final String VP8_MIME_TYPE = "video/x-vnd.on2.vp8";
  private static final String VP9_MIME_TYPE = "video/x-vnd.on2.vp9";
  private static final String H264_MIME_TYPE = "video/avc";
//private static final String H264_MIME_TYPE = "video/hevc";

  public static Context currentContext;

  // Class describing supported media codec properties.
  private static class MediaCodecProperties {
    public final String codecPrefix;
    // Minimum Android SDK required for this codec to be used.
    public final int minSdk;
    // Flag if encoder implementation does not use frame timestamps to calculate frame bitrate
    // budget and instead is relying on initial fps configuration assuming that all frames are
    // coming at fixed initial frame rate. Bitrate adjustment is required for this case.
    public final boolean bitrateAdjustmentRequired;

    MediaCodecProperties(
        String codecPrefix, int minSdk, boolean bitrateAdjustmentRequired) {
      this.codecPrefix = codecPrefix;
      this.minSdk = minSdk;
      this.bitrateAdjustmentRequired = bitrateAdjustmentRequired;
    }
  }

  // List of supported HW VP8 encoders.
  private static final MediaCodecProperties qcomVp8HwProperties = new MediaCodecProperties(
      "OMX.qcom.", Build.VERSION_CODES.KITKAT, false /* bitrateAdjustmentRequired */);
  private static final MediaCodecProperties exynosVp8HwProperties = new MediaCodecProperties(
      "OMX.Exynos.", Build.VERSION_CODES.M, false /* bitrateAdjustmentRequired */);
  private static final MediaCodecProperties[] vp8HwList = new MediaCodecProperties[] {
    qcomVp8HwProperties, exynosVp8HwProperties
  };

  // List of supported HW VP9 encoders.
  private static final MediaCodecProperties qcomVp9HwProperties = new MediaCodecProperties(
      "OMX.qcom.", Build.VERSION_CODES.M, false /* bitrateAdjustmentRequired */);
  private static final MediaCodecProperties exynosVp9HwProperties = new MediaCodecProperties(
      "OMX.Exynos.", Build.VERSION_CODES.M, false /* bitrateAdjustmentRequired */);
  private static final MediaCodecProperties[] vp9HwList = new MediaCodecProperties[] {
    qcomVp9HwProperties, exynosVp9HwProperties
  };

  // List of supported HW H.264 encoders.
  private static final MediaCodecProperties qcomH264HwProperties = new MediaCodecProperties(
      "OMX.qcom.", Build.VERSION_CODES.KITKAT, false /* bitrateAdjustmentRequired */);
  private static final MediaCodecProperties exynosH264HwProperties = new MediaCodecProperties(
      "OMX.Exynos.", Build.VERSION_CODES.LOLLIPOP, true /* bitrateAdjustmentRequired */);
  private static final MediaCodecProperties[] h264HwList = new MediaCodecProperties[] {
    qcomH264HwProperties, exynosH264HwProperties
  };

  // List of devices with poor H.264 encoder quality.
  private static final String[] H264_HW_EXCEPTION_MODELS = new String[] {
    // HW H.264 encoder on below devices has poor bitrate control - actual
    // bitrates deviates a lot from the target value.
    "SAMSUNG-SGH-I337",
    "Nexus 7",
    "Nexus 4"
  };

  // Bitrate modes - should be in sync with OMX_VIDEO_CONTROLRATETYPE defined
  // in OMX_Video.h
  private static final int VIDEO_ControlRateConstant = 2;
  // NV12 color format supported by QCOM codec, but not declared in MediaCodec -
  // see /hardware/qcom/media/mm-core/inc/OMX_QCOMExtns.h
  private static final int
    COLOR_QCOM_FORMATYUV420PackedSemiPlanar32m = 0x7FA30C04;
  // Allowable color formats supported by codec - in order of preference.
  private static final int[] supportedColorList = {
    CodecCapabilities.COLOR_FormatYUV420Planar,
    CodecCapabilities.COLOR_FormatYUV420SemiPlanar,
    CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar,
    COLOR_QCOM_FORMATYUV420PackedSemiPlanar32m
  };
  private static final int[] supportedSurfaceColorList = {
    CodecCapabilities.COLOR_FormatSurface
  };
  private VideoCodecType type;
  private int colorFormat;  // Used by native code.
  private boolean bitrateAdjustmentRequired;

  // SPS and PPS NALs (Config frame) for H.264.
  private ByteBuffer configData = null;

  // MediaCodec error handler - invoked when critical error happens which may prevent
  // further use of media codec API. Now it means that one of media codec instances
  // is hanging and can no longer be used in the next call.
  public static interface MediaCodecVideoEncoderErrorCallback {
    void onMediaCodecVideoEncoderCriticalError(int codecErrors);
  }

  public static void setErrorCallback(MediaCodecVideoEncoderErrorCallback errorCallback) {
    Logging.d(TAG, "Set error callback");
    MediaCodecVideoEncoder.errorCallback = errorCallback;
  }

  // Functions to disable HW encoding - can be called from applications for platforms
  // which have known HW decoding problems.
  public static void disableVp8HwCodec() {
    Logging.w(TAG, "VP8 encoding is disabled by application.");
    hwEncoderDisabledTypes.add(VP8_MIME_TYPE);
  }

  public static void disableVp9HwCodec() {
    Logging.w(TAG, "VP9 encoding is disabled by application.");
    hwEncoderDisabledTypes.add(VP9_MIME_TYPE);
  }

  public static void disableH264HwCodec() {
    Logging.w(TAG, "H.264 encoding is disabled by application.");
    hwEncoderDisabledTypes.add(H264_MIME_TYPE);
  }

  // Functions to query if HW encoding is supported.
  public static boolean isVp8HwSupported() {
    return !hwEncoderDisabledTypes.contains(VP8_MIME_TYPE) &&
        (findHwEncoder(VP8_MIME_TYPE, vp8HwList, supportedColorList) != null);
  }

  public static boolean isVp9HwSupported() {
    return !hwEncoderDisabledTypes.contains(VP9_MIME_TYPE) &&
        (findHwEncoder(VP9_MIME_TYPE, vp9HwList, supportedColorList) != null);
  }

  public static boolean isH264HwSupported() {
    return !hwEncoderDisabledTypes.contains(H264_MIME_TYPE) &&
        (findHwEncoder(H264_MIME_TYPE, h264HwList, supportedColorList) != null);
  }

  public static boolean isVp8HwSupportedUsingTextures() {
    return !hwEncoderDisabledTypes.contains(VP8_MIME_TYPE) &&
        (findHwEncoder(VP8_MIME_TYPE, vp8HwList, supportedSurfaceColorList) != null);
  }

  public static boolean isVp9HwSupportedUsingTextures() {
    return !hwEncoderDisabledTypes.contains(VP9_MIME_TYPE) &&
        (findHwEncoder(VP9_MIME_TYPE, vp9HwList, supportedSurfaceColorList) != null);
  }

  public static boolean isH264HwSupportedUsingTextures() {
    return !hwEncoderDisabledTypes.contains(H264_MIME_TYPE) &&
        (findHwEncoder(H264_MIME_TYPE, h264HwList, supportedSurfaceColorList) != null);
  }

  // Helper struct for findHwEncoder() below.
  private static class EncoderProperties {
    public EncoderProperties(String codecName, int colorFormat, boolean bitrateAdjustment) {
      this.codecName = codecName;
      this.colorFormat = colorFormat;
      this.bitrateAdjustment = bitrateAdjustment;
    }
    public final String codecName; // OpenMax component name for HW codec.
    public final int colorFormat;  // Color format supported by codec.
    public final boolean bitrateAdjustment; // true if bitrate adjustment workaround is required.
  }

  private static EncoderProperties findHwEncoder(
      String mime, MediaCodecProperties[] supportedHwCodecProperties, int[] colorList) {
    // MediaCodec.setParameters is missing for JB and below, so bitrate
    // can not be adjusted dynamically.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
      return null;
    }

    // Check if device is in H.264 exception list.
    if (mime.equals(H264_MIME_TYPE)) {
      List<String> exceptionModels = Arrays.asList(H264_HW_EXCEPTION_MODELS);
      if (exceptionModels.contains(Build.MODEL)) {
        Logging.w(TAG, "Model: " + Build.MODEL + " has black listed H.264 encoder.");
        return null;
      }
    }

    for (int i = 0; i < MediaCodecList.getCodecCount(); ++i) {
      MediaCodecInfo info = null;
      try {
        info = MediaCodecList.getCodecInfoAt(i);
      } catch (IllegalArgumentException e) {
        Logging.e(TAG,  "Cannot retrieve encoder codec info", e);
      }
      if (info == null || !info.isEncoder()) {
        continue;
      }
      String name = null;
      for (String mimeType : info.getSupportedTypes()) {
        if (mimeType.equals(mime)) {
          name = info.getName();
          break;
        }
      }
      if (name == null) {
        continue;  // No HW support in this codec; try the next one.
      }
      Logging.v(TAG, "Found candidate encoder " + name);

      // Check if this is supported HW encoder.
      boolean supportedCodec = false;
      boolean bitrateAdjustmentRequired = false;
      for (MediaCodecProperties codecProperties : supportedHwCodecProperties) {
        if (name.startsWith(codecProperties.codecPrefix)) {
          if (Build.VERSION.SDK_INT < codecProperties.minSdk) {
            Logging.w(TAG, "Codec " + name + " is disabled due to SDK version " +
                Build.VERSION.SDK_INT);
            continue;
          }
          if (codecProperties.bitrateAdjustmentRequired) {
            Logging.w(TAG, "Codec " + name + " does not use frame timestamps.");
            bitrateAdjustmentRequired = true;
          }
          supportedCodec = true;
          break;
        }
      }
      if (!supportedCodec) {
        continue;
      }

      // Check if HW codec supports known color format.
      CodecCapabilities capabilities;
      try {
        capabilities = info.getCapabilitiesForType(mime);
      } catch (IllegalArgumentException e) {
        Logging.e(TAG,  "Cannot retrieve encoder capabilities", e);
        continue;
      }
      for (int colorFormat : capabilities.colorFormats) {
        Logging.v(TAG, "   Color: 0x" + Integer.toHexString(colorFormat));
      }

      for (int supportedColorFormat : colorList) {
        for (int codecColorFormat : capabilities.colorFormats) {
          if (codecColorFormat == supportedColorFormat) {
            // Found supported HW encoder.
            Logging.d(TAG, "Found target encoder for mime " + mime + " : " + name +
                ". Color: 0x" + Integer.toHexString(codecColorFormat));
            return new EncoderProperties(name, codecColorFormat, bitrateAdjustmentRequired);
          }
        }
      }
    }
    return null;  // No HW encoder.
  }

  private void checkOnMediaCodecThread() {
    if (mediaCodecThread.getId() != Thread.currentThread().getId()) {
      throw new RuntimeException(
          "MediaCodecVideoEncoder previously operated on " + mediaCodecThread +
          " but is now called on " + Thread.currentThread());
    }
  }

  public static void printStackTrace() {
    if (runningInstance != null && runningInstance.mediaCodecThread != null) {
      StackTraceElement[] mediaCodecStackTraces = runningInstance.mediaCodecThread.getStackTrace();
      if (mediaCodecStackTraces.length > 0) {
        Logging.d(TAG, "MediaCodecVideoEncoder stacks trace:");
        for (StackTraceElement stackTrace : mediaCodecStackTraces) {
          Logging.d(TAG, stackTrace.toString());
        }
      }
    }
  }

  static MediaCodec createByCodecName(String codecName) {
    try {
      // In the L-SDK this call can throw IOException so in order to work in
      // both cases catch an exception.
      return MediaCodec.createByCodecName(codecName);
    } catch (Exception e) {
      return null;
    }
  }

  boolean initEncode(VideoCodecType type, int width, int height, int kbps, int fps,
      EglBase14.Context sharedContext) {
    final boolean useSurface = sharedContext != null;
    Logging.d(TAG, "Java initEncode: " + type + " : " + width + " x " + height +
        ". @ " + kbps + " kbps. Fps: " + fps + ". Encode from texture : " + useSurface);

    this.width = width;
    this.height = height;
    if (mediaCodecThread != null) {
      throw new RuntimeException("Forgot to release()?");
    }
    EncoderProperties properties = null;
    String mime = null;
    int keyFrameIntervalSec = 0;
    if (type == VideoCodecType.VIDEO_CODEC_VP8) {
      mime = VP8_MIME_TYPE;
      properties = findHwEncoder(
          VP8_MIME_TYPE, vp8HwList, useSurface ? supportedSurfaceColorList : supportedColorList);
      keyFrameIntervalSec = 100;
    } else if (type == VideoCodecType.VIDEO_CODEC_VP9) {
      mime = VP9_MIME_TYPE;
      properties = findHwEncoder(
          VP9_MIME_TYPE, vp9HwList, useSurface ? supportedSurfaceColorList : supportedColorList);
      keyFrameIntervalSec = 100;
    } else if (type == VideoCodecType.VIDEO_CODEC_H264) {
      mime = H264_MIME_TYPE;
      properties = findHwEncoder(
          H264_MIME_TYPE, h264HwList, useSurface ? supportedSurfaceColorList : supportedColorList);
      keyFrameIntervalSec = 20;
    }
    if (properties == null) {
      throw new RuntimeException("Can not find HW encoder for " + type);
    }
    runningInstance = this; // Encoder is now running and can be queried for stack traces.
    colorFormat = properties.colorFormat;
    bitrateAdjustmentRequired = properties.bitrateAdjustment;
    if (bitrateAdjustmentRequired) {
      fps = BITRATE_ADJUSTMENT_FPS;
    }
    Logging.d(TAG, "Color format: " + colorFormat +
        ". Bitrate adjustment: " + bitrateAdjustmentRequired);

    mediaCodecThread = Thread.currentThread();
    try {
      MediaFormat format = MediaFormat.createVideoFormat(mime, width, height);
      format.setInteger(MediaFormat.KEY_BIT_RATE, kbps);
      format.setInteger("bitrate-mode", VIDEO_ControlRateConstant);
      format.setInteger(MediaFormat.KEY_COLOR_FORMAT, properties.colorFormat);
      format.setInteger(MediaFormat.KEY_FRAME_RATE, fps);
      format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, keyFrameIntervalSec);
      Logging.d(TAG, "  Format: " + format);
      mediaCodec = createByCodecName(properties.codecName);
      this.type = type;
      if (mediaCodec == null) {
        Logging.e(TAG, "Can not create media encoder");
        return false;
      }
      mediaCodec.configure(
          format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

      if (useSurface) {
        eglBase = new EglBase14(sharedContext, EglBase.CONFIG_RECORDABLE);
        // Create an input surface and keep a reference since we must release the surface when done.
        inputSurface = mediaCodec.createInputSurface();
        eglBase.createSurface(inputSurface);
        drawer = new GlRectDrawer();
      }
      mediaCodec.start();
      outputBuffers = mediaCodec.getOutputBuffers();
      Logging.d(TAG, "Output buffers: " + outputBuffers.length);

    } catch (IllegalStateException e) {
      Logging.e(TAG, "initEncode failed", e);
      return false;
    }
    return true;
  }

  ByteBuffer[]  getInputBuffers() {
    ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
    Logging.d(TAG, "Input buffers: " + inputBuffers.length);
    return inputBuffers;
  }

  boolean encodeBuffer(
      boolean isKeyframe, int inputBuffer, int size,
      long presentationTimestampUs) {
    checkOnMediaCodecThread();
    try {
      if (isKeyframe) {
        // Ideally MediaCodec would honor BUFFER_FLAG_SYNC_FRAME so we could
        // indicate this in queueInputBuffer() below and guarantee _this_ frame
        // be encoded as a key frame, but sadly that flag is ignored.  Instead,
        // we request a key frame "soon".
        Logging.d(TAG, "Sync frame request");
        Bundle b = new Bundle();
        b.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0);
        mediaCodec.setParameters(b);
      }
      mediaCodec.queueInputBuffer(
          inputBuffer, 0, size, presentationTimestampUs, 0);
      return true;
    }
    catch (IllegalStateException e) {
      Logging.e(TAG, "encodeBuffer failed", e);
      return false;
    }
  }

  boolean encodeTexture(boolean isKeyframe, int oesTextureId, float[] transformationMatrix,
      long presentationTimestampUs) {
    checkOnMediaCodecThread();
    try {
      if (isKeyframe) {
        Logging.d(TAG, "Sync frame request");
        Bundle b = new Bundle();
        b.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0);
        mediaCodec.setParameters(b);
      }
      eglBase.makeCurrent();
      // TODO(perkj): glClear() shouldn't be necessary since every pixel is covered anyway,
      // but it's a workaround for bug webrtc:5147.
      GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
      drawer.drawOes(oesTextureId, transformationMatrix, width, height, 0, 0, width, height);
      eglBase.swapBuffers(TimeUnit.MICROSECONDS.toNanos(presentationTimestampUs));
      return true;
    }
    catch (RuntimeException e) {
      Logging.e(TAG, "encodeTexture failed", e);
      return false;
    }
  }

  void release() {
    Logging.d(TAG, "Java releaseEncoder");
    checkOnMediaCodecThread();

    // Run Mediacodec stop() and release() on separate thread since sometime
    // Mediacodec.stop() may hang.
    final CountDownLatch releaseDone = new CountDownLatch(1);

    Runnable runMediaCodecRelease = new Runnable() {
      @Override
      public void run() {
        try {
          Logging.d(TAG, "Java releaseEncoder on release thread");
          mediaCodec.stop();
          mediaCodec.release();
          Logging.d(TAG, "Java releaseEncoder on release thread done");
        } catch (Exception e) {
          Logging.e(TAG, "Media encoder release failed", e);
        }
        releaseDone.countDown();
      }
    };
    new Thread(runMediaCodecRelease).start();

    if (!ThreadUtils.awaitUninterruptibly(releaseDone, MEDIA_CODEC_RELEASE_TIMEOUT_MS)) {
      Logging.e(TAG, "Media encoder release timeout");
      codecErrors++;
      if (errorCallback != null) {
        Logging.e(TAG, "Invoke codec error callback. Errors: " + codecErrors);
        errorCallback.onMediaCodecVideoEncoderCriticalError(codecErrors);
      }
    }

    mediaCodec = null;
    mediaCodecThread = null;
    if (drawer != null) {
      drawer.release();
      drawer = null;
    }
    if (eglBase != null) {
      eglBase.release();
      eglBase = null;
    }
    if (inputSurface != null) {
      inputSurface.release();
      inputSurface = null;
    }
    runningInstance = null;
    Logging.d(TAG, "Java releaseEncoder done");
  }

  int getBitrate(){
    try {

      int bitrate = 0;
      if (null != currentContext){
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(currentContext);
        bitrate = settings.getInt("com.cloudminds.hc.hariservice.key.videoBps", 0);
      }
      return bitrate;

    }catch (Exception e){
      return 0;
    }
  }

  private boolean setRates(int kbps, int frameRate) {
    checkOnMediaCodecThread();
    int rate = getBitrate();
    kbps = rate>0?rate:kbps;
    int codecBitrate = 1000 * kbps;
    if (bitrateAdjustmentRequired && frameRate > 0) {
      codecBitrate = BITRATE_ADJUSTMENT_FPS * codecBitrate / frameRate;
      Logging.v(TAG, "setRates: " + kbps + " -> " + (codecBitrate / 1000)
          + " kbps. Fps: " + frameRate);
    } else {
      Logging.v(TAG, "setRates: " + kbps);
    }
    try {
      Bundle params = new Bundle();
      params.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, codecBitrate);
      //Mas to avoid below crash dump
      /*
      01 10:17:54.497 V/MediaCodecVideoEncoder(11348): [7032:495] [13447] MediaCodecVideoEncoder: setRates: 1536
01-01 10:17:54.503 W/System.err(11348): java.lang.NullPointerException: Attempt to invoke virtual method 'void android.media.MediaCodec.setParameters(android.os.Bundle)' on a null object reference
01-01 10:17:54.503 W/System.err(11348): 	at org.webrtc.MediaCodecVideoEncoder.setRates(MediaCodecVideoEncoder.java:539)
01-01 10:17:54.503 E/rtc     (11348):
01-01 10:17:54.503 E/rtc     (11348):
01-01 10:17:54.503 E/rtc     (11348): #
01-01 10:17:54.503 E/rtc     (11348): # Fatal error in ../../webrtc/api/android/jni/androidmediaencoder_jni.cc, line 907
01-01 10:17:54.503 E/rtc     (11348): # last system error: 88
01-01 10:17:54.503 E/rtc     (11348): # Check failed: !jni->ExceptionCheck()
01-01 10:17:54.503 E/rtc     (11348): #
01-01 10:17:54.503 E/rtc     (11348): #
01-01 10:17:54.503 F/libc    (11348): Fatal signal 6 (SIGABRT), code -6 in tid 13447 (MediaCodecVideo)
01-01 10:17:54.520 I/SurfaceViewRenderer(11348): [7032:518] [13257] SurfaceViewRenderer: local_video_view: No surface to draw on
01-01 10:17:54.557 I/DEBUG   (  315): *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** ***
01-01 10:17:54.557 I/DEBUG   (  315): Build fingerprint: 'qcom/apq8084/apq8084:5.0.2/LRX22G/xuzeyuan12081401:userdebug/test-keys'
01-01 10:17:54.557 I/DEBUG   (  315): Revision: '0'
01-01 10:17:54.557 I/DEBUG   (  315): ABI: 'arm'
01-01 10:17:54.558 I/DEBUG   (  315): pid: 11348, tid: 13447, name: MediaCodecVideo  >>> com.cloudminds.smartrobot <<<
01-01 10:17:54.558 I/DEBUG   (  315): signal 6 (SIGABRT), code -6 (SI_TKILL), fault addr --------
01-01 10:17:54.586 I/DEBUG   (  315):     r0 00000000  r1 00003487  r2 00000006  r3 00000000
01-01 10:17:54.586 I/DEBUG   (  315):     r4 97afddb8  r5 00000006  r6 00000058  r7 0000010c
01-01 10:17:54.586 I/DEBUG   (  315):     r8 0000000a  r9 97afdacc  sl 00000600  fp ffffffff
01-01 10:17:54.586 I/DEBUG   (  315):     ip 00003487  sp 97afda38  lr b6e8a0a1  pc b6eacff4  cpsr 60070010
01-01 10:17:54.587 I/DEBUG   (  315):
01-01 10:17:54.587 I/DEBUG   (  315): backtrace:
01-01 10:17:54.587 I/DEBUG   (  315):     #00 pc 00036ff4  /system/lib/libc.so (tgkill+12)
01-01 10:17:54.587 I/DEBUG   (  315):     #01 pc 0001409d  /system/lib/libc.so (pthread_kill+52)
01-01 10:17:54.587 I/DEBUG   (  315):     #02 pc 00014cbb  /system/lib/libc.so (raise+10)
01-01 10:17:54.587 I/DEBUG   (  315):     #03 pc 00011511  /system/lib/libc.so (__libc_android_abort+36)
01-01 10:17:54.587 I/DEBUG   (  315):     #04 pc 0000fc94  /system/lib/libc.so (abort+4)
01-01 10:17:54.587 I/DEBUG   (  315):     #05 pc 000bbfa7  /data/app/com.cloudminds.smartrobot-1/lib/arm/libjingle_peerconnection_so.so
01-01 10:17:54.587 I/DEBUG   (  315):     #06 pc 0007c3b9  /data/app/com.cloudminds.smartrobot-1/lib/arm/libjingle_peerconnection_so.so
01-01 10:17:54.587 I/DEBUG   (  315):     #07 pc 00079b55  /data/app/com.cloudminds.smartrobot-1/lib/arm/libjingle_peerconnection_so.so
01-01 10:17:54.587 I/DEBUG   (  315):     #08 pc 00334a21  /data/app/com.cloudminds.smartrobot-1/lib/arm/libjingle_peerconnection_so.so
01-01 10:17:54.587 I/DEBUG   (  315):     #09 pc 0031dcd9  /data/app/com.cloudminds.smartrobot-1/lib/arm/libjingle_peerconnection_so.so
01-01 10:17:54.587 I/DEBUG   (  315):     #10 pc 00334b79  /data/app/com.cloudminds.smartrobot-1/lib/arm/libjingle_peerconnection_so.so
01-01 10:17:54.587 I/DEBUG   (  315):     #11 pc 00335323  /data/app/com.cloudminds.smartrobot-1/lib/arm/libjingle_peerconnection_so.so
01-01 10:17:54.587 I/DEBUG   (  315):     #12 pc 00013887  /system/lib/libc.so (__pthread_start(void*)+30)
01-01 10:17:54.587 I/DEBUG   (  315):     #13 pc 0001187b  /system/lib/libc.so (__start_thread+6)
root@deskpc:/home/mas/voip/webrtc/src/out/Debug/lib# arm-linux-androideabi-addr2line -C -f -e libjingle_peerconnection_so.so 000bbfa7  0007c3b9 00079b55 00334a21 0031dcd9 00334b79 00335323
rtc::FatalMessage::~FatalMessage()
/home/mas/voip/webrtc/src/out/Debug/../../webrtc/base/checks.cc:109
webrtc_jni::MediaCodecVideoEncoder::SetRatesOnCodecThread(unsigned int, unsigned int)
/home/mas/voip/webrtc/src/out/Debug/../../webrtc/api/android/jni/androidmediaencoder_jni.cc:907 (discriminator 16)
rtc::MethodFunctor2<webrtc_jni::MediaCodecVideoEncoder, int (webrtc_jni::MediaCodecVideoEncoder::*)(unsigned int, unsigned int), int, unsigned int, unsigned int>::operator()() const
/home/mas/voip/webrtc/src/out/Debug/../../webrtc/base/bind.h:291 (discriminator 4)
rtc::Thread::ReceiveSendsFromThread(rtc::Thread const*)
/home/mas/voip/webrtc/src/out/Debug/../../webrtc/base/thread.cc:429
rtc::MessageQueue::Get(rtc::Message*, int, bool)
/home/mas/voip/webrtc/src/out/Debug/../../webrtc/base/messagequeue.cc:272
rtc::Thread::ProcessMessages(int)
/home/mas/voip/webrtc/src/out/Debug/../../webrtc/base/thread.cc:503
rtc::Thread::PreRun(void*)
/home/mas/voip/webrtc/src/out/Debug/../../webrtc/base/thread.cc:325

       */
      if(mediaCodec!=null)
      mediaCodec.setParameters(params);
      return true;
    } catch (IllegalStateException e) {
      Logging.e(TAG, "setRates failed", e);
      return false;
    }
  }

  // Dequeue an input buffer and return its index, -1 if no input buffer is
  // available, or -2 if the codec is no longer operative.
  int dequeueInputBuffer() {
    checkOnMediaCodecThread();
    try {
      return mediaCodec.dequeueInputBuffer(DEQUEUE_TIMEOUT);
    } catch (IllegalStateException e) {
      Logging.e(TAG, "dequeueIntputBuffer failed", e);
      return -2;
    }
  }

  // Helper struct for dequeueOutputBuffer() below.
  static class OutputBufferInfo {
    public OutputBufferInfo(
        int index, ByteBuffer buffer,
        boolean isKeyFrame, long presentationTimestampUs) {
      this.index = index;
      this.buffer = buffer;
      this.isKeyFrame = isKeyFrame;
      this.presentationTimestampUs = presentationTimestampUs;
    }

    public final int index;
    public final ByteBuffer buffer;
    public final boolean isKeyFrame;
    public final long presentationTimestampUs;
  }

  // Dequeue and return an output buffer, or null if no output is ready.  Return
  // a fake OutputBufferInfo with index -1 if the codec is no longer operable.
  OutputBufferInfo dequeueOutputBuffer() {
    checkOnMediaCodecThread();
    try {
      MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
      int result = mediaCodec.dequeueOutputBuffer(info, DEQUEUE_TIMEOUT);
      // Check if this is config frame and save configuration data.
      if (result >= 0) {
        boolean isConfigFrame =
            (info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0;
        if (isConfigFrame) {
          Logging.d(TAG, "Config frame generated. Offset: " + info.offset +
              ". Size: " + info.size);
          configData = ByteBuffer.allocateDirect(info.size);
          outputBuffers[result].position(info.offset);
          outputBuffers[result].limit(info.offset + info.size);
          configData.put(outputBuffers[result]);
          // Release buffer back.
          mediaCodec.releaseOutputBuffer(result, false);
          // Query next output.
          result = mediaCodec.dequeueOutputBuffer(info, DEQUEUE_TIMEOUT);
        }
      }
      if (result >= 0) {
        // MediaCodec doesn't care about Buffer position/remaining/etc so we can
        // mess with them to get a slice and avoid having to pass extra
        // (BufferInfo-related) parameters back to C++.
        ByteBuffer outputBuffer = outputBuffers[result].duplicate();
        outputBuffer.position(info.offset);
        outputBuffer.limit(info.offset + info.size);
        // Check key frame flag.
        boolean isKeyFrame =
            (info.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME) != 0;
        if (isKeyFrame) {
          Logging.d(TAG, "Sync frame generated");
        }
        if (isKeyFrame && type == VideoCodecType.VIDEO_CODEC_H264) {
          Logging.d(TAG, "Appending config frame of size " + configData.capacity() +
              " to output buffer with offset " + info.offset + ", size " +
              info.size);
          // For H.264 key frame append SPS and PPS NALs at the start
          ByteBuffer keyFrameBuffer = ByteBuffer.allocateDirect(
              configData.capacity() + info.size);
          configData.rewind();
          keyFrameBuffer.put(configData);
          keyFrameBuffer.put(outputBuffer);
          keyFrameBuffer.position(0);
          return new OutputBufferInfo(result, keyFrameBuffer,
              isKeyFrame, info.presentationTimeUs);
        } else {
          return new OutputBufferInfo(result, outputBuffer.slice(),
              isKeyFrame, info.presentationTimeUs);
        }
      } else if (result == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
        outputBuffers = mediaCodec.getOutputBuffers();
        return dequeueOutputBuffer();
      } else if (result == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
        return dequeueOutputBuffer();
      } else if (result == MediaCodec.INFO_TRY_AGAIN_LATER) {
        return null;
      }
      throw new RuntimeException("dequeueOutputBuffer: " + result);
    } catch (IllegalStateException e) {
      Logging.e(TAG, "dequeueOutputBuffer failed", e);
      return new OutputBufferInfo(-1, null, false, -1);
    }
  }

  // Release a dequeued output buffer back to the codec for re-use.  Return
  // false if the codec is no longer operable.
  boolean releaseOutputBuffer(int index) {
    checkOnMediaCodecThread();
    try {
      mediaCodec.releaseOutputBuffer(index, false);
      return true;
    } catch (IllegalStateException e) {
      Logging.e(TAG, "releaseOutputBuffer failed", e);
      return false;
    }
  }
}
