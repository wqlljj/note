/*
 *  Copyright 2016 The WebRTC project authors. All Rights Reserved.
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
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Range;
import android.view.Surface;
import android.view.WindowManager;
import org.siprop.android.uvccamera.UVCCameraAndroid;
import org.webrtc.CameraEnumerationAndroid.CaptureFormat;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class UsbCameraSession implements CameraSession {
  private static final String TAG = "smartUsbCameraSession";

  private static enum SessionState { RUNNING, STOPPED };

  private static final int NUMBER_OF_CAPTURE_BUFFERS = 5;

  private final Handler cameraThreadHandler;
  private final CameraVideoCapturer.CameraEventsHandler eventsHandler;
  private final Context applicationContext;
  private final CameraVideoCapturer.CapturerObserver capturerObserver;
  private final SurfaceTextureHelper surfaceTextureHelper;
  private final int cameraId;
  private final int width;
  private final int height;
  private final int framerate;

  // Initialized at start
  private CaptureFormat captureFormat;

  // Used only for stats. Only used on the camera thread.
  private static UVCCameraAndroid uvcCamera;
  private final long constructionTimeNs; // Construction time of this class.
  private final boolean captureToTexture;

  // Initialized when capture session is created
  private CameraCaptureSession captureSession;
  private CameraVideoCapturer.CameraStatistics cameraStatistics;

  // State
  private SessionState state = SessionState.RUNNING;
  private boolean firstFrameReported = false;

    public static void create(final CameraSession.CreateSessionCallback callback, final CameraVideoCapturer.CameraEventsHandler eventsHandler,
                              CameraVideoCapturer.CapturerObserver capturerObserver,final boolean captureToTexture, final Context applicationContext,
                              final SurfaceTextureHelper surfaceTextureHelper, final int cameraId, final int width,
                              final int height, final int framerate) {
      final long constructionTimeNs = System.nanoTime();
      Logging.d(TAG, "Open camera " + cameraId + " with width:"+width+", height:"+height+", fps:"+framerate);

      eventsHandler.onCameraOpening(cameraId);

      try {
        uvcCamera = UVCCameraAndroid.getInstance();
        uvcCamera.initFrameRate();
//      uvcCamera.setErrorCallback(new Camera.ErrorCallback() {
//        @Override
//        public void onError(int error, Camera camera) {
//          String errorMessage;
//          if (error == android.hardware.Camera.CAMERA_ERROR_SERVER_DIED) {
//            errorMessage = "Camera server died!";
//          } else {
//            errorMessage = "Camera error: " + error;
//          }
//          Logging.e(TAG, errorMessage);
//          if (callback != null) {
//            callback.onFailure(errorMessage);
//          }
//        }
//      });
        int ret = uvcCamera.open();
        if(ret!=0){
          callback.onFailure("UCS:Failed to open camera");
        }
      } catch (RuntimeException e) {
        callback.onFailure(e.getMessage());
        return;
      }
      if(uvcCamera==null){
        callback.onFailure("UVCCameraAndroid初始化失败");
        return;
      }

      final CaptureFormat captureFormat = new CaptureFormat(
              width, height,
              5,
              30);

      //set usb camera frame width/height and frame rate
      UVCCameraAndroid.Parameters params = uvcCamera.getParameters();
      params.setPreviewFrameRate(captureFormat.framerate.max);
      params.setPreviewSize(width, height);
      uvcCamera.setParameters(params);
      //set whether display frame for debug
      uvcCamera.setIsDisplay(false);

      // Initialize the capture buffers.
      if (!captureToTexture) {
        final int frameSize = captureFormat.frameSize();
        for (int i = 0; i < NUMBER_OF_CAPTURE_BUFFERS; ++i) {
          final ByteBuffer buffer = ByteBuffer.allocateDirect(frameSize);
          uvcCamera.addCallbackBuffer(buffer);
        }
      }

      callback.onDone(
              new UsbCameraSession(eventsHandler,capturerObserver, captureToTexture, applicationContext, surfaceTextureHelper,
                      cameraId, width, height, framerate, captureFormat, constructionTimeNs));
    }


  private UsbCameraSession(CameraVideoCapturer.CameraEventsHandler eventsHandler,CameraVideoCapturer.CapturerObserver capturerObserver,
                           boolean captureToTexture, Context applicationContext,
                           SurfaceTextureHelper surfaceTextureHelper, int cameraId, int width, int height, int framerate,
                           CaptureFormat captureFormat,long  constructionTimeNs) {
    Logging.d(TAG, "Create new usbcamera session on usbcamera " + cameraId);

    this.cameraThreadHandler = new Handler();
    this.eventsHandler = eventsHandler;
    this.captureToTexture = captureToTexture;
    this.applicationContext = applicationContext;
    this.capturerObserver = capturerObserver;
    this.surfaceTextureHelper = surfaceTextureHelper;
    this.cameraId = cameraId;
    this.width = width;
    this.height = height;
    this.framerate = framerate;
    this.captureFormat = captureFormat;
    this.constructionTimeNs = constructionTimeNs;

    start();
  }

  private void start() {
    checkIsOnCameraThread();
    Logging.d(TAG, "Start capturing");

    state = SessionState.RUNNING;

    uvcCamera.setErrorCallback(new android.hardware.Camera.ErrorCallback() {
      @Override
      public void onError(int error, android.hardware.Camera camera) {
        String errorMessage;
        if (error == android.hardware.Camera.CAMERA_ERROR_SERVER_DIED) {
          errorMessage = "Camera server died!";
        } else {
          errorMessage = "Camera error: " + error;
        }
        Logging.e(TAG, errorMessage);
        state = SessionState.STOPPED;
        stopInternal();
        if (error == android.hardware.Camera.CAMERA_ERROR_EVICTED) {
          eventsHandler.onCameraClosed();
        } else {
          eventsHandler.onCameraError(errorMessage);
        }
      }
    });

    if (captureToTexture) {
      //listenForTextureFrames();
    } else {
      listenForBytebufferFrames();
    }
    try {
      uvcCamera.startPreview();
    } catch (RuntimeException e) {
      state = SessionState.STOPPED;
      stopInternal();
      eventsHandler.onCameraError( e.getMessage());
    }
    capturerObserver.onCapturerStarted(true /* success */);
  }

  @Override
  public void stop() {
    Logging.d(TAG, "Stop usbcamera session on camera " + cameraId);

    if (Thread.currentThread() == cameraThreadHandler.getLooper().getThread()) {
      if (state != SessionState.STOPPED) {
        state = SessionState.STOPPED;
        capturerObserver.onCapturerStopped();
        // Post the stopInternal to return earlier.
        cameraThreadHandler.post(new Runnable() {
          @Override
          public void run() {
            stopInternal();
          }
        });
      }
    } else {
      final CountDownLatch stopLatch = new CountDownLatch(1);

      cameraThreadHandler.post(new Runnable() {
        @Override
        public void run() {
          if (state != SessionState.STOPPED) {
            state = SessionState.STOPPED;
            capturerObserver.onCapturerStopped();
            stopLatch.countDown();
            stopInternal();
          }
        }
      });

      ThreadUtils.awaitUninterruptibly(stopLatch);
    }
  }

  private void stopInternal() {

    Logging.d(TAG, "Stop internal");
    checkIsOnCameraThread();

    surfaceTextureHelper.stopListening();

    // Note: stopPreview or other driver code might deadlock. Deadlock in
    // android.hardware.Camera._stopPreview(Native Method) has been observed on
    // Nexus 5 (hammerhead), OS version LMY48I.
    uvcCamera.stopPreview();
    uvcCamera.release();
    uvcCamera.closeCamera();
    uvcCamera = null;
    eventsHandler.onCameraClosed();

    Logging.d(TAG, "Stop done");

  }

//  private void reportError(String error) {
//    checkIsOnCameraThread();
//    Logging.e(TAG, "Error: " + error);
//
//    if (captureSession == null) {
//      if (uvcCamera != null) {
//        uvcCamera.release();
//        uvcCamera.closeCamera();
//        uvcCamera = null;
//      }
//
//      state = SessionState.STOPPED;
//      callback.onFailure(error);
//      capturerObserver.onCapturerStarted(false /* success */);
//    } else {
//      eventsHandler.onCameraError(error);
//    }
//  }

  private int getDeviceOrientation() {
    int orientation = 0;

    WindowManager wm = (WindowManager) applicationContext.getSystemService(
        Context.WINDOW_SERVICE);
    switch(wm.getDefaultDisplay().getRotation()) {
      case Surface.ROTATION_90:
        orientation = 90;
        break;
      case Surface.ROTATION_180:
        orientation = 180;
        break;
      case Surface.ROTATION_270:
        orientation = 270;
        break;
      case Surface.ROTATION_0:
      default:
        orientation = 0;
        break;
    }
    return orientation;
  }

  private int getFrameOrientation() {
    int rotation = 0;// getDeviceOrientation();
    rotation = 180 - rotation;
    return (rotation ) % 360;
  }

  private void checkIsOnCameraThread() {
    if (Thread.currentThread() != cameraThreadHandler.getLooper().getThread()) {
      throw new IllegalStateException("Wrong thread");
    }
  }

  private void listenForBytebufferFrames() {
    uvcCamera.setPreviewCallbackWithBuffer(new UVCCameraAndroid.UCameraPreviewCallback() {
      @Override
      public void onUvcCameraPreviewFrame(final ByteBuffer byteBuffer, final UVCCameraAndroid callbackCamera) {

        cameraThreadHandler.post(new Runnable() {
          @Override
          public void run() {
            checkIsOnCameraThread();

            if (callbackCamera != uvcCamera) {
              Logging.e(TAG, "Callback from a different camera. This should never happen.");
              return;
            }

            if (state != SessionState.RUNNING) {
              Logging.d(TAG, "Bytebuffer frame captured but camera is no longer running.");
              return;
            }

            final long captureTimeNs = TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime());


            if (!firstFrameReported) {
              eventsHandler.onFirstFrameAvailable();
              firstFrameReported = true;
            }

            capturerObserver.onByteBufferFrameCaptured(byteBuffer.array(), captureFormat.width,
                    captureFormat.height, getFrameOrientation(), captureTimeNs);
            uvcCamera.addCallbackBuffer(byteBuffer);
          }
        });

      }
    });
  }
}
