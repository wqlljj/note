package com.cloudminds.meta.accesscontroltv.hcsdk;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.sax.RootElement;
import android.util.Log;
import android.view.SurfaceView;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.cloudminds.meta.accesscontroltv.view.MainActivity;
import com.hikvision.netsdk.ExceptionCallBack;
import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_DEVICEINFO_V30;
import com.hikvision.netsdk.NET_DVR_PREVIEWINFO;

/**
 * Created by WQ on 2018/4/24.
 */

public class HCClient implements ExceptionCallBack {
    private static String TAG="HCClient";
    private static HCNetSDK instance;
    private  SurfaceView playSurfaceView;
    private static HCClient hcClient;
    private NET_DVR_DEVICEINFO_V30 m_oNetDvrDeviceInfoV30;
    private  Activity activity;
    private int m_iStartChan;
    private int m_iChanNum;
    private int m_iLogID = -1;
    private int m_iPlayID = -1;
    private int m_iPlaybackID = -1;

    public static HCClient initHCSDK(Activity activity, SurfaceView playSurfaceView){
        if(hcClient==null)
        hcClient = new HCClient();
        hcClient.activity = activity;
        hcClient.playSurfaceView = playSurfaceView;
        instance = HCNetSDK.getInstance();
        if (instance.NET_DVR_Init()){
            Log.e(TAG,"=====初始化海康sdk成功====");
            instance.NET_DVR_SetExceptionCallBack(hcClient);
        }else {
            Log.e(TAG,"=====初始化海康sdk失败====");
        }
        HCNetSDK.getInstance().NET_DVR_SetLogToFile(3, Environment.getExternalStorageDirectory()+"/sdklog/", true);
        return hcClient;
    }

//    loginNormalDevice("10.11.36.204","8000","admin","dt111111");
    public int loginNormalDevice() {
        m_oNetDvrDeviceInfoV30 = new NET_DVR_DEVICEINFO_V30();
        if (null == m_oNetDvrDeviceInfoV30) {
            Log.e(TAG, "HKNetDvrDeviceInfoV30 new is failed!");
            return m_iLogID =-1;
        }
        String strIP = MainActivity.isDebug?"10.11.36.204":"10.11.36.203";
        int nPort = Integer.parseInt("8000");
        String strUser = "admin";
        String strPsd = "dt111111";
        int iLogID = HCNetSDK.getInstance().NET_DVR_Login_V30(strIP, nPort,strUser, strPsd, m_oNetDvrDeviceInfoV30);
        if (iLogID < 0) {
            Log.e(TAG, "====登陆失败==" + HCNetSDK.getInstance().NET_DVR_GetLastError());
//            preview.setText("登陆失败");
            return m_iLogID =-1;
        }
        Toast.makeText(activity, "登陆成功", Toast.LENGTH_SHORT).show();
        Log.e(TAG,"========登陆成功===");
        Log.e(TAG,"======on======"+m_oNetDvrDeviceInfoV30.byChanNum);
        if (m_oNetDvrDeviceInfoV30.byChanNum > 0) {
            m_iStartChan = m_oNetDvrDeviceInfoV30.byStartChan;
            m_iChanNum = m_oNetDvrDeviceInfoV30.byChanNum;
        } else if (m_oNetDvrDeviceInfoV30.byIPChanNum > 0) {
            Log.e("============","======down======");
            m_iStartChan = m_oNetDvrDeviceInfoV30.byStartDChan;
            m_iChanNum = 1;
        }
        return m_iLogID =iLogID;
    }

    public void preview(){
        try {
            ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }catch (Exception e){
            Log.e(TAG,"=======error====");
            e.printStackTrace();
        }

        if (m_iLogID < 0) {
            Log.e(TAG, "====请先登录====");
            return;
        }
        if (m_iPlaybackID >= 0) {
            Log.i(TAG, "=====请先退出之前账号====");
            return;
        }
        if (m_iPlayID < 0) {
            startSinglePreview();
//            preview.setText("预览中");
            Toast.makeText(activity, "预览中", Toast.LENGTH_SHORT).show();
        } else {
            stopSinglePreview();
//            preview.setText("预览");
            Toast.makeText(activity, "预览", Toast.LENGTH_SHORT).show();
        }
    }

    private void startSinglePreview() {
        NET_DVR_PREVIEWINFO previewInfo = new NET_DVR_PREVIEWINFO();
        previewInfo.lChannel = m_iStartChan;
        previewInfo.dwStreamType = 0;
        previewInfo.bBlocked = 1;
        previewInfo.hHwnd = playSurfaceView.getHolder();
        m_iPlayID = HCNetSDK.getInstance().NET_DVR_RealPlay_V40(m_iLogID, previewInfo, null);
        if (m_iPlayID < 0) {
            Log.e("=======", "NET_DVR_RealPlay is failed!Err:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return;
        }
        m_iPlayID = HCNetSDK.getInstance().NET_DVR_RealPlay_V40(m_iLogID,
                previewInfo, null);
    }

    public void stopSinglePreview() {
        if (m_iPlayID < 0) {
            Log.e(TAG, "m_iPlayID < 0");
            return;
        }
        if (!HCNetSDK.getInstance().NET_DVR_StopRealPlay(m_iPlayID)) {
            Log.e(TAG, "StopRealPlay is failed!Err:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return;
        }
        m_iPlayID = -1;
    }

    public void stopPlayBack(){
        if (!HCNetSDK.getInstance().NET_DVR_StopPlayBack(m_iPlaybackID)) {
            Log.e(TAG, "net sdk stop playback failed");
        }
        m_iPlaybackID = -1;
    }

    @Override
    public void fExceptionCallBack(int i, int i1, int i2) {

        Log.e(TAG, "fExceptionCallBack: " +  i+ " " + i1 + "  " + i2);
        if (this.m_iPlaybackID != -1) {
            mHandler.sendEmptyMessage(100);
        }
    }
    Handler mHandler = new Handler()
    {
        public void handleMessage(Message paramAnonymousMessage)
        {
            super.handleMessage(paramAnonymousMessage);
            switch (paramAnonymousMessage.what)
            {
                case 100:
                    if (HCClient.this.loginNormalDevice() != -1)
                    {
                        HCClient.this.preview();
                        return;
                    }
                    Log.e(HCClient.TAG, "handleMessage:登录失败");
                    HCClient.this.mHandler.sendEmptyMessageDelayed(100, 5000L);
                    break;
                default:
                    return;
            }

        }
    };
}
