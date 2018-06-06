package com.kongqw;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.R;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * Created by WQ on 2018/4/16.
 */

public class OpenCVApi {

    private static String TAG="OPENCV/OpenCVApi";
    private static ObjectDetector mFaceDetector;
    private static int status;

    public static void init(final Context context) {
        BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(context) {
            @Override
            public void onManagerConnected(int status) {
                OpenCVApi.status = status;
                if (OpenCVApi.status == LoaderCallbackInterface.SUCCESS) {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mFaceDetector = new ObjectDetector(context, R.raw.haarcascade_frontalface_alt_tree_1, 6, 0.2F, 0.2F, new Scalar(255, 0, 0, 255));
                } else {
                    super.onManagerConnected(status);
                }
            }
        };
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, context, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
    public static int detectFaceNum(Bitmap bitmap) {
        try {
            // bitmapToMat
            Mat toMat = new Mat();
            Utils.bitmapToMat(bitmap, toMat);
            Mat copyMat = new Mat();
            toMat.copyTo(copyMat); // 复制

            // togray
            Mat gray = new Mat();
            Imgproc.cvtColor(toMat, gray, Imgproc.COLOR_RGBA2GRAY);

            MatOfRect mRect = new MatOfRect();
            Rect[] object = mFaceDetector.detectObjectImage(gray, mRect);

            Log.e("detectFace objectLength", object.length + "");


            int maxRectArea = 0 * 0;
            Rect maxRect = null;

            int facenum = 0;
            // Draw a bounding box around each face.
            for (Rect rect : object) {
                Imgproc.rectangle(
                        toMat,
                        new Point(rect.x, rect.y),
                        new Point(rect.x + rect.width, rect.y + rect.height),
                        new Scalar(255, 0, 0), 3);
                ++facenum;
                // 找出最大的面积
                int tmp = rect.width * rect.height;
                if (tmp >= maxRectArea) {
                    maxRectArea = tmp;
                    maxRect = rect;
                }
            }

//            rectBitmap = null;
//            if (facenum != 0) {
                // 剪切最大的头像
//                Log.e("detectFace剪切的长宽", String.format("高:%s,宽:%s", maxRect.width, maxRect.height));
//                Rect rect = new Rect(maxRect.x, maxRect.y, maxRect.width, maxRect.height);
//                Mat rectMat = new Mat(copyMat, rect);  // 从原始图像拿
//                rectBitmap = Bitmap.createBitmap(rectMat.cols(), rectMat.rows(), Bitmap.Config.ARGB_8888);
//                Utils.matToBitmap(rectMat, rectBitmap);
//            }

            Log.e("detectFace", "detectFace: "+String.format("检测到%1$d个人脸", facenum));
            //绘制识别区域
//            Utils.matToBitmap(toMat, bitmap);
            return facenum;
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if(rectBitmap!=null){
//            ((ImageView) findViewById(R.id.imageView)).setImageBitmap(rectBitmap);
//        }
        return 0;
    }
}
