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
import android.hardware.camera2.CameraManager;
import org.webrtc.CameraEnumerationAndroid.CaptureFormat;

import java.util.List;

public class UsbCameraCapturer extends CameraCapturer {

  private final boolean captureToTexture;

  public UsbCameraCapturer(
          UsbCameraEnumerator emulator, String cameraName, CameraEventsHandler eventsHandler, boolean captureToTexture) {
    super(cameraName, eventsHandler, emulator);

    this.captureToTexture = captureToTexture;
  }

  @Override
  public List<CaptureFormat> getSupportedFormats() {
    return UsbCameraEnumerator.getSupportedFormats();
  }


  @Override
  protected void createCameraSession(
          CameraSession.CreateSessionCallback createSessionCallback,
          CameraEventsHandler eventsHandler, Context applicationContext,
          CapturerObserver capturerObserver,
           SurfaceTextureHelper surfaceTextureHelper, String cameraName, int width, int height,
                                     int framerate) {
    try {
      UsbCameraSession.create(createSessionCallback, eventsHandler, capturerObserver, captureToTexture, applicationContext,
              surfaceTextureHelper, UsbCameraEnumerator.getCameraIndex(cameraName), width, height,
              framerate);
    }catch (IllegalArgumentException e){
      eventsHandler.onCameraError(e.getMessage());
    }catch (Exception e){
      createSessionCallback.onFailure("UsbCameraSession create fail");
    }

  }
}
