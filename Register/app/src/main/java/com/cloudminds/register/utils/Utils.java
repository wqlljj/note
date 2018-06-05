package com.cloudminds.register.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.util.Patterns;

import com.cloudminds.register.BasicApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class Utils {

    public static final String TAG = "Register";

    public static File getVisitorImage() {
        return getTempImage("visitor.jpg");
    }

    public static File getEmployeeImage() {
        return getTempImage("employee.jpg");
    }

    public static File getSignImage() {
        return getTempImage("sign.png");
    }

    private static File getTempImage(String fileName) {
        File imagePath = new File(BasicApp.getContext().getFilesDir(), "images");
        if (!imagePath.exists()) {
            imagePath.mkdirs();
        }
        File tempFile = new File(imagePath, fileName);
        Log.d(TAG, "temp image = " + tempFile);
        return tempFile;
    }

    public static Bitmap decodeSampledBitmapFromResource(
            String pathName, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static int genderConvert(String gender) {
        switch (gender) {
            case "Male":
                return 0;
            case "Female":
                return 1;
            default:
                return 0;
        }
    }

    public static String convertGender(int position) {
        switch (position) {
            case 0:
                return "Male";
            case 1:
                return "Female";
            default:
                return "Male";
        }
    }

    public static int liveConvert(String live) {
        switch (live) {
            case "Yes":
                return 0;
            case "No":
                return 1;
            default:
                return 0;
        }
    }

    public static String convertLive(int position) {
        switch (position) {
            case 0:
                return "Yes";
            case 1:
                return "No";
            default:
                return "Yes";
        }
    }

    public static int positionConvert(String address) {
        switch (address) {
            case "PK":
                return 0;
            case "CD":
                return 1;
            case "SZ":
                return 2;
            case "FS":
                return 3;
            default:
                return 0;
        }
    }

    public static String convertPosition(int position) {
        switch (position) {
            case 0:
                return "PK";
            case 1:
                return "CD";
            case 2:
                return "SZ";
            case 3:
                return "FS";
            default:
                return "PK";
        }
    }


    public static boolean isPhone(String phone) {
        return Patterns.PHONE.matcher(phone).matches();
    }

    public static boolean isEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static long getFileSizeLong(File paramFile)
            throws Exception
    {
        if (paramFile.exists()) {
            return new FileInputStream(paramFile).available();
        }
        paramFile.createNewFile();
        Log.e("Register", "获取文件大小失败!");
        return 0L;
    }
    public static String getFileSizeString(File paramFile)
            throws Exception
    {
        return formetFileSize(getFileSizeLong(paramFile));
    }

    private static String formetFileSize(long paramLong)
    {
        Object localObject = new DecimalFormat("#.00");
        if (paramLong == 0L) {
            return "0B";
        }
        if (paramLong < 1024L) {
            localObject = ((DecimalFormat)localObject).format(paramLong) + "B";
        }else{
            if (paramLong < 1048576L) {
                localObject = ((DecimalFormat)localObject).format(paramLong / 1024.0D) + "KB";
            } else if (paramLong < 1073741824L) {
                localObject = ((DecimalFormat)localObject).format(paramLong / 1048576.0D) + "MB";
            } else {
                localObject = ((DecimalFormat)localObject).format(paramLong / 1.073741824E9D) + "GB";
            }
        }
        return (String)localObject;
    }
    public static String saveBitmap(Bitmap paramBitmap, File paramFile, int paramInt)
    {
        try
        {
            FileOutputStream localFileOutputStream = new FileOutputStream(paramFile);
            paramBitmap.compress(Bitmap.CompressFormat.JPEG, paramInt, localFileOutputStream);
            localFileOutputStream.flush();
            localFileOutputStream.close();
            return paramFile.getAbsolutePath();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static String hanziToPinyin(String source) {
        Log.d(TAG, "hanziToPinyin ------- = " + source);
        ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance().get(source);
        StringBuilder sb = new StringBuilder();
        for (HanziToPinyin.Token token : tokens) {
            Log.d(TAG, "token.source  = " + token.source);
            Log.d(TAG, "token.target  = " + token.target);
            sb.append(token.target);
        }
        return sb.toString().toLowerCase();
    }

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            // 获取手机所有连接管理对象(包括对wi-fi,net等连接的管理)
            ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            // 获取NetworkInfo对象
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            //判断NetworkInfo对象是否为空
            if (networkInfo != null)
                return networkInfo.isConnected();
        }
        return false;
    }

}
