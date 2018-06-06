package org.siprop.android.uvccamera;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
//import com.cloudminds.smartrobot.utils.USBUtils;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.ThreadUtils;

//import cn.yunzhisheng.tts.offline.FileUtil;

public class UVCCameraAndroid {
    private static final boolean DEBUG = true;
    private static final String TAG = "smartUvcCamera";
    private static UVCCameraAndroid usbCamera = null;

    private HandlerThread cameraThread = null;
    private Handler cameraThreadHandler = null;

//    private boolean cameraExists = false;
//    private boolean shouldStop = false;
    private UCameraPreviewCallback cameracapture = null;
    private final static String OV580 = "OV580";
    private final static int INVALID_CAMERA_ID = -1;
    private Parameters curCameraParameter;
//    private boolean isUsbCameraSupported = false;
    // /dev/videoX with Required 666 permission, default id is 2
    private int cameraId=INVALID_CAMERA_ID; //5 is verified on A1; 2 is for 8084
//    public static final int PREVIEW_WIDTH = 1280;//640;
//    public static final int PREVIEW_HEIGHT = 480;
//    public static final int PREVIEW_FPS = 25;

    private static  int CAMERA_CAPTURE_INTERVAL = 15;//30;
    public static Context currentContext = null;

    public static final int CAMERA_REOPEN_INTERVAL = 500;//500ms
    private volatile boolean reopening = false;

    Camera.ErrorCallback errorCallback = null;
    protected Context context;

    //frame bufs
    private final List<ByteBuffer> queuedBuffers = new ArrayList<ByteBuffer>();
    // JNI functions
    public native int openCamera(int videoid);
    public native int setParameters(int width, int height, int rate);
    public native int startCamera();
    public native void setCallbackBuffer(Object buffer);
    public native int processCamera();
    public native void stopCamera();
    public native int getCameraState();
    public native void closeCamera();
    public native void pixeltobmp(Bitmap bitmap);
    public native void setIsDisplay(boolean bdisplay);
    public native int getErrorCode();
    public native Object[] enumV4L2Device();
    public native Object[] enumV4L2CaptureFormat(int videoid);


    public static final int ERROR_LOCAL = -1;
    public static final int SUCCESS_LOCAL = 0;
    public static final int ERROR_CODE_NOERROR = 0;
    public static final int ERROR_CODE_NOMAL = 1;
    public static final int ERROR_CODE_DEVICE_LOST = 2;
    public static final int ERROR_CODE_DEVICE_REPEATOPEN = 3;
    public static final int ERROR_CODE_DEVICE_OPEN_FAILED = 4;
    public static final int ERROR_CODE_STATE_ERROR = 5;
    public static final int ERROR_CODE_SETFORMAT_ERROR = 6;
    public static final int ERROR_CODE_SETRATE_ERROR = 7;
    public static final int ERROR_CODE_INITMAP_ERROR = 8;

    public static final int USBCAMERA_STATE_IDLE = 0;
    public static final int USBCAMERA_STATE_OPENED = 1;
    public static final int USBCAMERA_STATE_CAPTURING = 2;
    public static final int USBCAMERA_STATE_STOPED = 3;

    static {
        System.loadLibrary("UVCCamera");
    }

//    IntentFilter usbDeviceStateFilter = new IntentFilter();
//    private void setFilteraction(){
//        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
//        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
//        context.registerReceiver(mUsbReceiver, usbDeviceStateFilter);
//    }
//
//    BroadcastReceiver  mUsbReceiver = new BroadcastReceiver(){
//        public void onReceive(Context context, Intent intent){
//            String action = intent.getAction();
//            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
//                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
//                if (device != null) {
//                    String name = device.getDeviceName();
//                    Log.d(TAG,"DETACHED usb name is " + name);
//                }
//            }
//        }
//    };


    public static UVCCameraAndroid getInstance(){
        if (usbCamera == null) {
            usbCamera = new UVCCameraAndroid();
        }
//        usbCamera.initFrameRate();
        return usbCamera;
    }

    public void initFrameRate(){
        try {
            cameracapture = null;
            queuedBuffers.clear();
            cameraId=INVALID_CAMERA_ID;
//        setFilteraction();
            Log.e(TAG, "initFrameRate: " );
            scanUsbCamera();
            int framerate = 0;
            if (null != currentContext){
                final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(currentContext);
                framerate = settings.getInt("com.cloudminds.hc.hariservice.key.videoFps", 0);
            }
            if (framerate>0){
                Log.e(TAG, "initFrameRate: "+framerate );
                CAMERA_CAPTURE_INTERVAL = 1000/framerate;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public UVCCameraAndroid() {
        if(DEBUG) Log.d(TAG,"UvcCamera constructed");
    }

    public void initWithContext(Context context){
        this.context = context;
    }

    public  int getCameraId(){
        Log.e(TAG, "getCameraId: " );
        scanUsbCamera();
        return cameraId;
    }

    //check whether support usb camera,default is not support
    public boolean isUSBCameraSupport() {
        Log.e(TAG, "isUSBCameraSupport: " );
        scanUsbCamera();
        return  cameraId!=INVALID_CAMERA_ID;
    }


    private final Runnable reopenCameraCallback = new Runnable() {
        @Override
        public void run() {
            try {
                int cameraId = getCameraId();
                closeCamera();
                openCamera(cameraId);
                if (curCameraParameter != null) {
                    Log.e(TAG, "curCameraParameter: 1"+curCameraParameter.imagewidth+" "+ curCameraParameter.imageheight+" "+curCameraParameter.framerate);
                    setParameters(curCameraParameter.imagewidth, curCameraParameter.imageheight, curCameraParameter.framerate);
                }else{
                    Log.e(TAG, "curCameraParameter: null 1");
                }
                startCamera();
                if (cameraThreadHandler != null) {
                    cameraThreadHandler.removeCallbacks(reopenCameraCallback);
                }
            }catch (Exception e){
                Log.e(TAG,"Failed reopenCamera due to "+e.getLocalizedMessage());
            }
            reopening =  false;
        }
    };

    private void reopenCameraWithDelay(final int delay){
        if(cameraThreadHandler!=null) {
            if(!reopening) {
                reopening = true;
                cameraThreadHandler.removeCallbacks(reopenCameraCallback);
                cameraThreadHandler.postDelayed(reopenCameraCallback, delay);
            }
        }
    }

    private final Runnable captureOneFrame = new Runnable() {
        @Override
        public void run() {
            try {
                //notify videoCaptureAndroid to dail with frame buffer
                if (queuedBuffers.size() > 0) {
                    ByteBuffer byteBuffer = queuedBuffers.get(0);
                    setCallbackBuffer(byteBuffer);
                    //process camera
                    int ret = processCamera();
                    if(ret==0) {
                        byte[] buf = byteBuffer.array();
                        queuedBuffers.remove(0);
                        //if (DEBUG) Log.d(TAG, "uvccamera read a frame with size = "+ buf.length + " and remove byteBuffer "+byteBuffer+" from the queue ["+queuedBuffers.size()+"]");

                        if (cameracapture != null) {
                            cameracapture.onUvcCameraPreviewFrame(byteBuffer, UVCCameraAndroid.this);
                        }
                    }else{
                        if(ret==-1){
                            int error = getErrorCode();
                            Log.e(TAG, "Failed to read a frame due to ErrorCode:"+error);
                            if(error==ERROR_CODE_DEVICE_LOST){
                                Log.e(TAG, "Device has been removed");
                            }

                            reopenCameraWithDelay(5);

                        }else {
                            if (DEBUG)
                                Log.d(TAG, "uvccamera:: camera thread could run slower little bit, where queuebuffer has more slots[" + queuedBuffers.size() + "] but no new frame");
                        }
                    }
                } else {
                    //shall we drop the cached frames

                    setCallbackBuffer(null);
                    //process camera
                    int ret = processCamera();
                    if(ret==0)
                    {
                        if (DEBUG) Log.d(TAG, "uvccamera a frame is dropped because queuebuffer has no slot, maybe we shall slow down the FPS of the camera");
                    }else{
                        if(ret==-1){
                            int error = getErrorCode();
                            Log.e(TAG, "Failed to read a frame due to ErrorCode:"+error);
                            if(error==ERROR_CODE_DEVICE_LOST){
                                Log.e(TAG, "Device has been removed");
                            }
                            reopenCameraWithDelay(CAMERA_CAPTURE_INTERVAL);
                        }else {
                            if (DEBUG)
                                Log.d(TAG, "uvccamera:: camera thread could run slower little bit, where queuebuffer is empty and no new frame");
                        }
                    }
                }
            }catch (Exception e){
                Log.e(TAG,"Failed to capture one frame due to "+e.getLocalizedMessage());
            }
            if(cameraThreadHandler!=null)
                cameraThreadHandler.postDelayed(captureOneFrame, CAMERA_CAPTURE_INTERVAL);
        }
    };


    public int open(/*int id*/){
        // /dev/videoX
        int ret = openCamera(cameraId);
        if(ret != 0){
            getCameraState();
            if(DEBUG) Log.d(TAG,"Failed to open camera");
            if(errorCallback!=null) errorCallback.onError(Camera.CAMERA_ERROR_UNKNOWN,null);
        }
        return ret;
    }


//    public void convertPixeltobmp(Bitmap bitmap){
//        if (bitmap != null){
//            pixeltobmp(bitmap);
//        }
//    }
//    public void setNeedDisplay(boolean display){
//        setIsDisplay(display);
//    }

    public void setErrorCallback(Camera.ErrorCallback errorCallback) {
        this.errorCallback = errorCallback;
    }

    //set callback with buffer
    public void setPreviewCallbackWithBuffer(UCameraPreviewCallback capture){
    	cameracapture = capture;
    }
    
    //add callback buffer, it used to return frame data buffer
    public void addCallbackBuffer(final ByteBuffer callbackBuffer){
        if (callbackBuffer == null) {
            return;
        }
        if(cameraThreadHandler==null){
            queuedBuffers.add(callbackBuffer);
        }else {
            cameraThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    queuedBuffers.add(callbackBuffer);
                }
            });
        }
    }
    
    public void setParameters(Parameters cameraParameter){
    	if (cameraParameter != null){
            curCameraParameter = cameraParameter;
            Log.e(TAG, "curCameraParameter: "+curCameraParameter.imagewidth+" "+ curCameraParameter.imageheight+" "+curCameraParameter.framerate);
            setParameters(cameraParameter.imagewidth,cameraParameter.imageheight,cameraParameter.framerate);
    	}else{
            Log.e(TAG, "curCameraParameter: null");

        }
    }
    
    public Parameters getParameters(){
    	Parameters cameraParameters = null;
    	cameraParameters = new Parameters();
    	return cameraParameters;
    }
    
    //start usb camera thread and start preview
    public boolean startPreview(){
        //create a thread to process camera data

        int ret = startCamera();
        if (ret != 0) {
            if(DEBUG) Log.d(TAG,"Failed to start camera");
            if(errorCallback!=null) errorCallback.onError(Camera.CAMERA_ERROR_UNKNOWN,null);
            return false;
        }

        cameraThread = new HandlerThread(TAG);
        cameraThread.setPriority(Thread.MAX_PRIORITY);
        cameraThread.start();
        cameraThreadHandler = new Handler(cameraThread.getLooper());
        Log.e(TAG, "startPreview: "+CAMERA_CAPTURE_INTERVAL );
        cameraThreadHandler.post(captureOneFrame);

    	return true;
    }
    
    //stop camera preview
    public void stopPreview(){

        if(cameraThreadHandler!=null) {
            cameraThreadHandler.removeCallbacks(captureOneFrame);
            cameraThreadHandler=null;
        }


        if(cameraThread!=null) {
            cameraThread.quitSafely();
            ThreadUtils.joinUninterruptibly(cameraThread);
            cameraThread = null;
        }

        setCallbackBuffer(null);
        stopCamera();

    }
    
    public void release(){
        closeCamera();
    	cameracapture = null;
    	queuedBuffers.clear();
    }

    public void scanUsbCamera(){
        List<V4L2Device> deviceList = enumDeviceList();
        if (deviceList != null && deviceList.size()>0){
//            for (int i=0;i<deviceList.size();i++){
//                if(DEBUG) Log.d(TAG,"Fund camera:"+deviceList.get(i).name+", id:"+deviceList.get(i).ID);
//            }
            cameraId = deviceList.get(0).ID;
        }
    }

    public int getDeviceCount(){
        List<V4L2Device> deviceList = enumDeviceList();
        if (deviceList != null){
            return deviceList.size();
        }
        return 0;
    }

    public List<V4L2Device> enumDeviceList(){
        int i = 0;
        List<V4L2Device> deviceList = new ArrayList<V4L2Device>();
        Log.d(TAG,"device list length is: deviceList" );
        Object[] myList = enumV4L2Device();
        Log.d(TAG,"device list length is: deviceList"+ Arrays.toString(myList) );
        if (myList != null){
            if(true) Log.d(TAG,"device list length is: " + myList.length);
            for (i = 0; i < myList.length; i++) {
                V4L2Device device = (V4L2Device)myList[i];
                deviceList.add(device);
                if(true) Log.d(TAG,"device name:" + device.name + "  device ID:" + device.ID);
            }
        }
        return deviceList;
    }

    public List<V4L2CaptureFormat> enumCaptureFormat(int videoid){
        int i = 0;
        List<V4L2CaptureFormat> formatList = new ArrayList<V4L2CaptureFormat>();
        Object[] myList = enumV4L2CaptureFormat(videoid);
        if (myList != null){
            if(true) Log.d(TAG,"capture format list length is: " + myList.length);
            for (i = 0; i < myList.length; i++) {
                V4L2CaptureFormat format = (V4L2CaptureFormat)myList[i];
                formatList.add(format);
                if(true) Log.d(TAG,"captureformat format:" + format.imageFormat +
                        "  width: " + format.width +
                        " height: " + format.height +
                        " maxrate: " + format.maxFramerate +
                        " minrate: " + format.minFramerate);
            }
        }
        return formatList;
    }


    public interface UCameraPreviewCallback {
        void onUvcCameraPreviewFrame(ByteBuffer var1, UVCCameraAndroid var2);
    }


    public class Parameters{
        //camera format width/height/frame rate
        public int imagewidth = 0;
        public int imageheight = 0;
        public int framerate = 0;
        public int imageFormat = ImageFormat.YV12;
        
        public void setPreviewSize(final int width, final int height){
        	imagewidth = width;
        	imageheight = height;
        }
        
        public void setPreviewFrameRate(final int rate){
        	framerate = rate;
        }
        
        public void setPreviewFormat(final int format){
        	imageFormat = format;
        }
    }


//    {"width":640,"height":480,"framerate":30},{"width":1280,"height":480,"framerate":30},{"width":1280,"height":720,"framerate":30},{"width":2560,"height":720,"framerate":30},{"width":1920,"height":1080,"framerate":30},{"width":3840,"height":1080,"framerate":30

    public static List<CameraEnumerationAndroid.CaptureFormat> getSupportedFormats(){
        final List<CameraEnumerationAndroid.CaptureFormat> formatList = new ArrayList<CameraEnumerationAndroid.CaptureFormat>();
        {
            CameraEnumerationAndroid.CaptureFormat format = new CameraEnumerationAndroid.CaptureFormat(640, 480,30000, 30000);
            formatList.add(format);
            format = new CameraEnumerationAndroid.CaptureFormat(640,480,5000, 30000);
            formatList.add(format);
            format = new CameraEnumerationAndroid.CaptureFormat(1280,480,5000, 30000);
            formatList.add(format);
            format = new CameraEnumerationAndroid.CaptureFormat(1280, 720,5000, 30000);
            formatList.add(format);
            format = new CameraEnumerationAndroid.CaptureFormat(2560,720,5000, 30000);
            formatList.add(format);
            format = new CameraEnumerationAndroid.CaptureFormat(1920,1080,5000, 30000);
            formatList.add(format);
            format = new CameraEnumerationAndroid.CaptureFormat(3840,1080,5000, 30000);
            formatList.add(format);
        }
        return formatList;
    }


}
