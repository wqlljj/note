package com.cloudminds.hc.metalib.features.network.downloading;

import android.os.Environment;


import com.cloudminds.hc.metalib.features.network.BaseNetworkTask;
import com.cloudminds.hc.metalib.utils.DLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Request;

/**
 * Created by willzhang on 16/06/17
 */

public class PackageDownloader extends BaseNetworkTask {

    private File packageFile;
    private boolean hasBreakPoint;
    private OnProgressListener mOnProgressListener;
    private DownloadCallback mDownloadCallback;

    private boolean needCancel = false;
    private boolean canceledByUser = false;

    interface DownloadCallback {
        void onDownloadFinished(int responseCode);
    }

    interface OnProgressListener {
        void onProgressUpdated(long total, long progressed);
    }

    public static File getPackageFolder() {
//        Environment.getExternalStorageDirectory()
//        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        File packageFolder = new File(Environment.getExternalStorageDirectory(), FOLDER_NAME);
        if (!packageFolder.exists()) {
            boolean created = packageFolder.mkdirs();
            if (!created) {
                DLog.e("Cannot create package folder:" + packageFolder);
            }
        }
        return packageFolder;
    }

    public static File getPackageFile(String fileName) {
        return new File(getPackageFolder(), fileName);
    }

    void download(String fileUrl, String fileName, OnProgressListener mOnProgressListener, DownloadCallback mDownloadCallback) {
        this.mOnProgressListener = mOnProgressListener;
        this.mDownloadCallback = mDownloadCallback;
        packageFile = getPackageFile(fileName);
        get(fileUrl);
    }

    @Override
    protected void setAdditionalParams(Request.Builder builder) throws IOException {
        if (packageFile.exists()) { // If exists, start from break point
            long fileLen = packageFile.length();
            String values = "bytes=" + fileLen + "-";
            builder.addHeader("RANGE", values);
            hasBreakPoint = true;
        } else {
            boolean created = packageFile.createNewFile();
            if (!created) {
                DLog.e("Cannot create file:" + packageFile);
            }
        }
    }

    @Override
    protected int parseData(long contentLength, InputStream is) {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(packageFile, hasBreakPoint);
            long fileLen = packageFile.length();
            long total = contentLength + fileLen;
            byte[] buffer = new byte[2048];
            int read;
            long received = hasBreakPoint ? fileLen : 0;
            while ((read = is.read(buffer)) > 0) {
                if (needCancel) {
                    return RESPONSE_CODE_CANCEL;
                }
                os.write(buffer, 0, read);
                received += read;
                if (null != mOnProgressListener) {
                    mOnProgressListener.onProgressUpdated(total, received);
                }
            }
            return RESPONSE_CODE_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != os) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return RESPONSE_CODE_REQUEST_FAILED;
    }

    @Override
    protected void onWorkFinished(int responseCode) {
        if (null != mDownloadCallback) {
            mDownloadCallback.onDownloadFinished(responseCode);
        }
    }

    public void setNeedCancel(boolean needCancel) {
        setNeedCancel(needCancel, false);
    }

    public void setNeedCancel(boolean needCancel, boolean canceledByUser) {
        this.needCancel = needCancel;
        this.canceledByUser = canceledByUser;
    }

    public boolean isCanceledByUser() {
        return canceledByUser;
    }
}
