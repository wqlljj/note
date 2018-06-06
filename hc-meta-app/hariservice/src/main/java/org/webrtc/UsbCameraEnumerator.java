/*
 *  Copyright 2015 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package org.webrtc;

import android.os.SystemClock;
import android.util.Log;

import org.siprop.android.uvccamera.UVCCameraAndroid;
import org.webrtc.CameraEnumerationAndroid.CaptureFormat;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class UsbCameraEnumerator implements CameraEnumerator {
  private final static String TAG = "UsbCameraEnumerator";
  // Each entry contains the supported formats for corresponding camera index. The formats for all
  // cameras are enumerated on the first call to getSupportedFormats(), and cached for future
  // reference.
  private static List<List<CaptureFormat>> cachedSupportedFormats;

  private final boolean captureToTexture;

  public UsbCameraEnumerator() {
    this(true /* captureToTexture */);
  }

  public UsbCameraEnumerator(boolean captureToTexture) {
    this.captureToTexture = captureToTexture;
  }

  // Returns device names that can be used to create a new VideoCapturerAndroid.
  @Override
  public String[] getDeviceNames() {

    try {
      if (UVCCameraAndroid.getInstance().isUSBCameraSupport()){
        String name = getDeviceName(UVCCameraAndroid.getInstance().getCameraId());
        String[] deviceNames = new String[1];
        deviceNames[0]=name;
        return deviceNames;
      }else{
        Logging.w(TAG, "There is no UVC camera attached");
        return new String[] {};
      }
    } catch (/* CameraAccessException */ Exception e) {
      Logging.e(TAG, "Camera access exception: " + e);
      return new String[] {};
    }

  }

  @Override
  public boolean isFrontFacing(String deviceName) {
    return true;
  }

  @Override
  public boolean isBackFacing(String deviceName) {
    return false;
  }

  @Override
  public CameraVideoCapturer createCapturer(String deviceName,
      CameraVideoCapturer.CameraEventsHandler eventsHandler) {
    return new UsbCameraCapturer(this, deviceName, eventsHandler, captureToTexture);
  }


  static synchronized List<CaptureFormat> getSupportedFormats() {
    int cameraId = 0;
    if (cachedSupportedFormats == null) {
      cachedSupportedFormats = new ArrayList<List<CaptureFormat>>();
//      for (int i = 0; i < android.hardware.Camera.getNumberOfCameras(); ++i)
      if(UVCCameraAndroid.getInstance().isUSBCameraSupport())
      {
        int i = UVCCameraAndroid.getInstance().getCameraId();
        cachedSupportedFormats.add(enumerateFormats(i));
      }
    }
    return cachedSupportedFormats.get(cameraId);
  }

  private static List<CaptureFormat> enumerateFormats(int cameraId) {

    Logging.d(TAG, "Get supported formats for camera index " + cameraId + ".");
    final long startTimeMs = SystemClock.elapsedRealtime();


    List<CaptureFormat> formatList = new ArrayList<CaptureFormat>();
    if(UVCCameraAndroid.getInstance().isUSBCameraSupport()){
      formatList =UVCCameraAndroid.getSupportedFormats();
    }

    final long endTimeMs = SystemClock.elapsedRealtime();
    Logging.d(TAG, "Get supported formats for camera index " + cameraId + " done."
            + " Time spent: " + (endTimeMs - startTimeMs) + " ms.");
    return formatList;

  }

//  // Convert from android.hardware.Camera.Size to Size.
//  static List<Size> convertSizes(List<android.hardware.Camera.Size> cameraSizes) {
//    final List<Size> sizes = new ArrayList<Size>();
//    for (android.hardware.Camera.Size size : cameraSizes) {
//      sizes.add(new Size(size.width, size.height));
//    }
//    return sizes;
//  }
//
//  // Convert from int[2] to CaptureFormat.FramerateRange.
//  static List<CaptureFormat.FramerateRange> convertFramerates(List<int[]> arrayRanges) {
//    final List<CaptureFormat.FramerateRange> ranges = new ArrayList<CaptureFormat.FramerateRange>();
//    for (int[] range : arrayRanges) {
//      ranges.add(new CaptureFormat.FramerateRange(
//          range[android.hardware.Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
//          range[android.hardware.Camera.Parameters.PREVIEW_FPS_MAX_INDEX]));
//    }
//    return ranges;
//  }

  // Returns the camera index for camera with name |deviceName|, or throws IllegalArgumentException
  // if no such camera can be found.
  static int getCameraIndex(String deviceName) {
    Logging.d(TAG, "getCameraIndex: " + deviceName);
//    for (int i = 0; i < android.hardware.Camera.getNumberOfCameras(); ++i)
    if(UVCCameraAndroid.getInstance().isUSBCameraSupport())
    {
      return UVCCameraAndroid.getInstance().getCameraId();
//      if (deviceName.equals(getDeviceName(i))) {
//        return i;
//      }
    }
    throw new IllegalArgumentException("No such camera: " + deviceName);
  }

  // Returns the name of the camera with camera index. Returns null if the
  // camera can not be used.
  static String getDeviceName(int index) {
    if (UVCCameraAndroid.getInstance().isUSBCameraSupport()){
      String name = "UsbCamera " + "0" + ", Facing " + "back"
              + ", Orientation " + "0";
      int cameraId = UVCCameraAndroid.getInstance().getCameraId();
      if(index==cameraId) return name;
    }
    return "";
  }

  public static boolean isSupported() {

    boolean supported = false;
    try {
      UVCCameraAndroid uvcCamera = UVCCameraAndroid.getInstance();
      Log.e(TAG, "isSupported: " );
      uvcCamera.scanUsbCamera();
      supported = uvcCamera.isUSBCameraSupport();
      Logging.d(TAG, "UvcCamera isUsbCameraDetected=" + supported);
    }catch (Exception e){
      e.printStackTrace();
    }finally {
      return supported;
    }
  }
}
