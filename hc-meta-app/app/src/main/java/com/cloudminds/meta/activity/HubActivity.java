package com.cloudminds.meta.activity;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.cloudminds.hc.hariservice.HariServiceClient;
import com.cloudminds.hc.hariservice.call.CallEngine;
import com.cloudminds.hc.metalib.CMUpdaterActivity;
import com.cloudminds.hc.metalib.USBUtils;
import com.cloudminds.hc.metalib.utils.ToastUtil;
import com.cloudminds.meta.R;
import com.cloudminds.meta.application.MetaApplication;
import com.cloudminds.meta.bean.ChatMessage;
import com.cloudminds.meta.broadcast.BatteryReceiver;
import com.cloudminds.meta.broadcast.InternetBroadcast;
import com.cloudminds.meta.broadcast.SelfStartUpBroadcast;
import com.cloudminds.meta.constant.Constant;
import com.cloudminds.meta.manager.ActivityManager;
import com.cloudminds.meta.presenter.HubPresenter;
import com.cloudminds.meta.service.AIBridge;
import com.cloudminds.meta.service.asr.BusEvent;
import com.cloudminds.meta.util.DangerousPermissions;
import com.cloudminds.meta.util.PlayerUtil;
import com.cloudminds.meta.util.SharePreferenceUtils;
import com.cloudminds.meta.util.TTSSpeaker;
import com.cloudminds.meta.view.HubDialog;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;
import pub.devrel.easypermissions.EasyPermissions;


//import static com.cloudminds.hc.hariservice.call.CallEngine.Callee.HARI_CALLEE_AI;
//import static com.cloudminds.hc.hariservice.call.CallEngine.Callee.HARI_CALLEE_HI;
import static com.cloudminds.meta.application.MetaApplication.state;
import static com.cloudminds.meta.constant.Constant.HUB_CONN_IN_CONNECTION;
import static com.cloudminds.meta.constant.Constant.HUB_CONN_ON_CONNECTION;

/**
 * Created by tiger on 17-4-5.
 */

public class HubActivity extends StandardActivity implements IHubView
        , View.OnClickListener
        , USBUtils.MetaHotSwapListener, EasyPermissions.PermissionCallbacks, SelfStartUpBroadcast.DoubleClickHomeListener {

    private static final String TAG = "Meta/HubActivity";
    private Button mCall;
    private TextView mState;
    private HubPresenter mPresenter;
    private HubDialog mDialog;
    private TextView mHistory;
    private CheckBox mNo_Meta;
    private PlayerUtil playerUtil;
    private InternetBroadcast internetBroadcast;
    private String[] pers;
    private TextView mMenu;
    private BatteryReceiver batteryReceiver;

    private final int CHANGECALLBUTTONENABLE = 100;//改变呼叫按钮可用性
    private final int PERMISSIONREQUESTCODE = 101;//权限请求
    private final int CHECKROMREQUESTCODE = 102;//检测头盔Rom是否更新
    private final int CHECKRESTARTCALL = 103;//检测启动后是否呼叫

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate: ");
        MetaApplication.isStartHubActivity = true;
        no_meta = SharePreferenceUtils.getPrefBoolean(Constant.PRE_KEY_NO_META, false);

        SelfStartUpBroadcast.isStartUp = true;
        setContentView(R.layout.hub_activity);
        internetBroadcast = new InternetBroadcast();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(internetBroadcast, filter);
        batteryReceiver = new BatteryReceiver();
        filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);
        PackageInfo pack = null;
        try {
            pack = getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        pers = pack.requestedPermissions;
        ArrayList<String> temp = new ArrayList<>();
        for (String per : pers) {
            if (DangerousPermissions.needrequest.contains(per)) {
                if (!EasyPermissions.hasPermissions(getApplicationContext(), per)) {
                    temp.add(per);
                }
            }
        }
        pers = temp.toArray(new String[temp.size()]);
        SelfStartUpBroadcast.setDoubleClickHomeListener(this);
        Log.d(TAG, "oncreate ---");
    }

    @Subscribe(sticky = false, priority = 0, threadMode = ThreadMode.MainThread)
    public void onEventBus(BusEvent ttsEvent) {
        switch (ttsEvent.getEvent()) {
            case MOVETASKTOBACK:
//                finish();
                break;
        }
    }

    boolean isInit=false;
    @Override
    protected void onResume() {
        Log.e(TAG, "onResume: ");
        if(!isInit) {
            isInit=true;
            if (!EasyPermissions.hasPermissions(this, pers)) {
                EasyPermissions.requestPermissions(this, "有些必要的权限未开启，不开启这些权限将会导致Meta不能正常工作，是否开启这些权限？", PERMISSIONREQUESTCODE, pers);
                super.onResume();
                return;
            } else {
                checkMetaRom();
                Log.e(TAG, "onCreate: 已获取权限");
                mPresenter = new HubPresenter(this);
                initViews();
                mPresenter.init();
//            AIBridge.instance(this.getApplicationContext());
                if (no_meta != mNo_Meta.isChecked()) {
                    no_meta = !no_meta;
                    metaPattern();
                }
                if (MetaApplication.state != HUB_CONN_ON_CONNECTION && MetaApplication.state != HUB_CONN_IN_CONNECTION) {
                    sendHandlerMessage(CHECKRESTARTCALL, 1200);
                }
            }
        }
        Class<?> currentActivity = ActivityManager.getCurrentActivity();
        if (currentActivity != null && !currentActivity.getName().equals(this.getClass().getName())) {
            startActivity(new Intent(this, currentActivity));
        } else {
            ActivityManager.setCurrentActivity(this.getClass());
        }
        super.onResume();
    }

    private void checkMetaRom() {
        if (!MetaApplication.isCheckRom) {
            Intent intent = new Intent(this, CMUpdaterActivity.class);
            startActivityForResult(intent, CHECKROMREQUESTCODE);
            MetaApplication.isCheckRom = true;
        }
    }

    @Override
    public void finish() {
        Log.e(TAG, "finish: ");
        ActivityManager.removeCurrentActivity(this.getClass());
        super.finish();
    }

    @Override
    public void startActivity(Intent intent) {
        ActivityManager.removeCurrentActivity(this.getClass());
        super.startActivity(intent);
    }

    boolean no_meta = false;

    private void initViews() {
        mCall = (Button) findViewById(R.id.hub_call_btn);
        mHistory = (TextView) findViewById(R.id.history);
        mMenu = (TextView) findViewById(R.id.menu);
        mState = (TextView) findViewById(R.id.hub_state);
        mNo_Meta = ((CheckBox) findViewById(R.id.no_Meta));
        findViewById(R.id.auxiliary_function).setOnClickListener(this);
        findViewById(R.id.restart_voice_trigger).setOnClickListener(this);
        mCall.setOnClickListener(this);
        mHistory.setOnClickListener(this);
        mMenu.setOnClickListener(this);
        mNo_Meta.setOnClickListener(this);
        Log.e(TAG, "initViews: ");
        setCallButtonEnable(false);
        playerUtil = PlayerUtil.getPlayerUtil(this);
        setStateByUsb((MetaApplication.state == Constant.HUB_CONN_IN_CONNECTION || MetaApplication.state == Constant.HUB_CONN_ON_CONNECTION) ? true : mPresenter.checkUsb());
        Log.e(TAG, "initViews: state  " + (state == HUB_CONN_IN_CONNECTION || state == HUB_CONN_ON_CONNECTION));
        if (state == HUB_CONN_IN_CONNECTION || state == HUB_CONN_ON_CONNECTION) {
            mPresenter.updateState(state);
            setUIState(state, null);
        }
    }

    @Override
    public void setStateByUsb(boolean conn) {
        Log.d(TAG, "current state +" + conn);
        conn = no_meta ? no_meta : conn;
        Log.d(TAG, "current state =" + conn);
        if (conn) {
            if (MetaApplication.state != HUB_CONN_ON_CONNECTION && MetaApplication.state != HUB_CONN_IN_CONNECTION) {
                mState.setVisibility(View.INVISIBLE);
            }
            setCallButtonEnable(true);
        } else {
            mState.setVisibility(View.VISIBLE);
            mState.setText(R.string.usb_status);
            mState.setTextColor(getResources().getColor(R.color.red));
            setCallButtonEnable(false);
        }
    }

    @Override
    public void setStateByCurrentMeta(boolean current) {
        if (current) {
            Log.e(TAG, "setStateByCurrentMeta: " + current);
            mCall.setEnabled(true);
            checkPermission();
        } else {
            showDialog();
        }
    }

    public CallEngine.Callee callCallee = CallEngine.getHARI_CALLEE_AI();
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.e(TAG, "handleMessage: " + msg.what);
            switch (msg.what) {
                case CHANGECALLBUTTONENABLE:
                    if (mCall != null && (mPresenter.checkUsb() || no_meta)) {
                        mCall.setEnabled(true);
                    } else if (!(mPresenter.checkUsb() || no_meta)) {
                        setStateByUsb(false);
                    }
                    break;
                case CHECKRESTARTCALL:
                    Log.e(TAG, "handleMessage: 102");
                    boolean startCall = getIntent().getBooleanExtra("startCall", false);
                    if (!startCall) {
                        startCall = SharePreferenceUtils.getPrefBoolean(Constant.PRE_KEY_STARTCALL, false);
                    }
                    Log.e(TAG, "onCreate: startCall = " + startCall);
                    if (startCall) {
                        if (!mPresenter.checkUsb() && !no_meta) {
                            metaPattern();
                        }
                        callCallee = SharePreferenceUtils.getPrefInt(Constant.PRE_KEY_CALLCALLEE, 0) == 0 ? CallEngine.getHARI_CALLEE_AI() : CallEngine.getHARI_CALLEE_HI();
                        SharePreferenceUtils.setPrefBoolean(Constant.PRE_KEY_STARTCALL, false);
                        mPresenter.doNext();
                    }
                    break;
            }
        }
    };

    private void sendHandlerMessage(int what, long delay) {
        if (what == CHANGECALLBUTTONENABLE) {
            if (mHandler.hasMessages(CHANGECALLBUTTONENABLE)) {
                mHandler.removeMessages(CHANGECALLBUTTONENABLE);
            }
        }
        mHandler.sendEmptyMessageDelayed(what, delay);
    }

    private void setCallButtonEnable(boolean flag) {
        Log.e(TAG, "setCallButtonEnable: " + flag);
        if (mHandler.hasMessages(CHANGECALLBUTTONENABLE)) {
            mHandler.removeMessages(CHANGECALLBUTTONENABLE);
        }
        mCall.setEnabled(flag);
    }

    @Override
    public synchronized void setUIState(int state, String message) {
        Log.d(TAG, "setUistate " + state + "  " + message);
        if (!TextUtils.isEmpty(message)) {
            MetaApplication.addMessage(ChatMessage.Type.CHAT_LEFT, message);
            ToastUtil.show(this.getApplicationContext(), message);
        }
        mState.setTextColor(getResources().getColor(R.color.black));
        switch (state) {
            case Constant.CALL_CLOSED:
                sendHandlerMessage(CHANGECALLBUTTONENABLE, 3500);
                break;
            case Constant.HUB_CONN_DISCONNECT:
                if (mCall != null) {
                    setCallButtonEnable(false);
                    sendHandlerMessage(CHANGECALLBUTTONENABLE, 8000);
                }
                mState.setVisibility(View.VISIBLE);
                mState.setText(R.string.hub_disconning);
                break;
            case HUB_CONN_IN_CONNECTION:
                setCallButtonEnable(true);
                mState.setVisibility(View.VISIBLE);
                mState.setText(R.string.hub_in_connection);
                mCall.setText(R.string.hub_disconnection);
                if (!MetaApplication.isReConnect) {
                    MetaApplication.isReConnect = true;
                    AuxiliaryFunctionActivity.listener = this;
                    startActivity(new Intent(this, AuxiliaryFunctionActivity.class));
                }
                break;
            case HUB_CONN_ON_CONNECTION:
                mState.setVisibility(View.VISIBLE);
                mState.setText(R.string.hub_calling);
                mCall.setText(R.string.hub_disconnection);
                break;
            case Constant.HUB_CONN_NOMAL:
                Log.e(TAG, "setUIState: " + (mCall != null) + "  " + (message != null));
                if (mCall != null && message != null) {
                    Log.e(TAG, "setUIState: mCall = true");
                    setCallButtonEnable(false);
                    sendHandlerMessage(CHANGECALLBUTTONENABLE, 8000);
                }
                MetaApplication.isReConnect = false;
                if (!TextUtils.isEmpty(message)) {
                    mState.setVisibility(View.VISIBLE);
                    mState.setText(message);
                } else {
                    mState.setVisibility(View.INVISIBLE);
                }
                if (message == null && !no_meta && !mPresenter.checkUsb()) {
                    if (mHandler.hasMessages(CHANGECALLBUTTONENABLE)) {
                        mHandler.removeMessages(CHANGECALLBUTTONENABLE);
                    }
                    setStateByUsb(false);
                    return;
                }
                mCall.setText(R.string.call_hari);
                break;
            case Constant.HUB_CONN_END:
                if (mCall != null) {
                    setCallButtonEnable(false);
                    sendHandlerMessage(CHANGECALLBUTTONENABLE, 8000);
                }
                MetaApplication.isReConnect = false;
                mCall.setText(R.string.call_hari);
                mState.setVisibility(View.VISIBLE);
                mState.setText(R.string.hub_call_end);
                break;
            case Constant.CALL_FAILED:
                if (mCall != null) {
                    setCallButtonEnable(false);
                    sendHandlerMessage(CHANGECALLBUTTONENABLE, 8000);
                }
                MetaApplication.isReConnect = false;
                mCall.setText(R.string.call_hari);
                mState.setVisibility(View.VISIBLE);
                mState.setText(R.string.hub_call_end);
                break;
        }
    }

    private void showDialog() {
        HubDialog.Builder builder = new HubDialog.Builder();
        mDialog = builder.setTitle(R.string.hub_dialog_title)
                .setCancle(R.string.btn_cancle)
                .setOk(R.string.btn_unbind)
                .setCancleListener(this)
                .setOkListener(this)
                .builder();
        mDialog.show(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (MetaApplication.isApplicationBroughtToBackground(this)) {
            Log.e(TAG, "onPause: true");
            ToastUtil.cancel();
        } else {
            Log.e(TAG, "onPause: false");
        }
    }

    @IdRes
    int btId;
    long clickTime = 0l;
    //检测是否重复点击
    private boolean checkReclick(@IdRes int id, long timeLimit) {
        Log.e(TAG, "checkReclick: " + id + "  " + timeLimit);
        if (btId == id && System.currentTimeMillis() - clickTime < timeLimit) {
//            ToastUtil.show(this, R.string.repetitive_operation);
            Log.e(TAG, "checkReclick: true");
            return true;
        }
        Log.e(TAG, "checkReclick: false");
        btId = id;
        clickTime = System.currentTimeMillis();
        return false;
    }

    @Override
    public void onClick(View view) {
        Log.e(TAG, "onClick: ");
        switch (view.getId()) {
            case R.id.hub_call_btn:
                if (checkReclick(R.id.hub_call_btn, 1000)) return;
//                Toast.makeText(this, ""+angle, Toast.LENGTH_SHORT).show();
//                HCMetaUtils.setLibSensorNaviExtend(angle%180);
//                angle+=45;
//                TTSSpeaker.speak("RMB 100 YUAN",TTSSpeaker.HIGH);
//                CrashReport.testJavaCrash();
//                int a=10/0;
//                RestartAPPTool.restartAPP(this);

//                TTSSpeaker.speak("Hello world!2018,tian an men ，故宫 长城 \n What's this? unkown.",TTSSpeaker.HIGH);

//                                JSONObject data = new JSONObject();
//                                JSONObject json = new JSONObject();
//                                try {
////                                    data.put("type",2);
//                                    json.put("type","volumeCtrlRecognize");
//                                    json.put("data",data);
//                                }catch (Exception e){
//                                    e.printStackTrace();
//                                }
//                                Log.e(TAG, "onMenuItemClick: volumeCtrlRecognize" );
//                                HariServiceClient.getCommandEngine().sendData(json);

//                startActivity(new Intent(this, Demo.class));
//                SnowboyClient.init(this);
//                SnowboyClient instance = SnowboyClient.getInstance();
//                if(instance.isRecording()) {
//                    instance.stopRecording();
//                }else{
//                    instance.startRecording();
//                }

                if (mPresenter.getState() == HUB_CONN_ON_CONNECTION || mPresenter.getState() == HUB_CONN_IN_CONNECTION) {
                    Log.e(TAG, "onClick: 挂断");
                    setCallButtonEnable(false);
                } else {
                    Log.e(TAG, "onClick: 连接");
                    setCallButtonEnable(false);
                    sendHandlerMessage(CHANGECALLBUTTONENABLE, 1000);
                }
                callCallee = CallEngine.getHARI_CALLEE_AI();
                mPresenter.doNext();
                break;
            case R.id.history:
                if (checkReclick(R.id.history, 1000)) return;
                Log.e(TAG, "onClick: history");
                startBaseActivity(BaseActivity.HISTORYRECORDS);
                break;
            case R.id.menu:
                if (checkReclick(R.id.menu, 1000)) return;
                Log.e(TAG, "onClick: menu");
                PopupMenu popup = new PopupMenu(this, findViewById(R.id.menu));
                popup.getMenuInflater()
                        .inflate(R.menu.main_popup_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.set:
                                if (checkReclick(R.id.set, 1000)) return true;
                                startBaseActivity(BaseActivity.SETTING);
                                break;
                            case R.id.hub_family_help:
                                if (checkReclick(R.id.hub_family_help, 1000)) return true;
                                doFamilyHelp();
                                break;
//                            case R.id.romUpdate:
//                                Toast.makeText(HubActivity.this, R.string.being_developed, Toast.LENGTH_SHORT).show();
//                                Intent intent=new Intent(HubActivity.this,CMUpdaterActivity.class);
//                                intent.putExtra("start_type","use");
//                                startActivity(intent);
                            default:
                                return true;
                        }
                        return true;
                    }
                });
                popup.show();
                break;
            case R.id.hub_dialog_cancle:
                if (checkReclick(R.id.hub_dialog_cancle, 1000)) return;
                Log.e(TAG, "onClick: hub_dialog_cancle");
                if (mDialog != null) mDialog.dismiss();
                mDialog = null;
                break;
            case R.id.hub_dialog_unbind:
                if (checkReclick(R.id.hub_dialog_unbind, 1000)) return;
                Log.e(TAG, "onClick: hub_dialog_unbind");
                mPresenter.unbindMeta();
                if (mDialog != null) mDialog.dismiss();
                mDialog = null;
                break;
            case R.id.no_Meta:
                Log.e(TAG, "onClick: no_Meta");
                if (MetaApplication.state != HUB_CONN_IN_CONNECTION && MetaApplication.state != HUB_CONN_ON_CONNECTION) {
                    metaPattern();
                } else {
                    mNo_Meta.setChecked(no_meta);
                    ToastUtil.show(this.getApplicationContext(), R.string.calling_bt_available);
                }
                break;
            case R.id.restart_voice_trigger:
                if (checkReclick(R.id.restart_voice_trigger, 1000)) return;
                Log.e(TAG, "onClick: restart_voice_trigger");
                if (MetaApplication.state == HUB_CONN_IN_CONNECTION) {
                    EventBus.getDefault().post(new BusEvent(BusEvent.Event.STOP_WAKEUPASR));
                    EventBus.getDefault().post(new BusEvent(BusEvent.Event.START_WAKEUPASR));
                } else {
                    TTSSpeaker.speak(getString(R.string.bt_unavailable), TTSSpeaker.HIGH);
                    ToastUtil.show(this.getApplicationContext(), R.string.bt_unavailable);
                }
                break;
            case R.id.auxiliary_function:
                if (checkReclick(R.id.auxiliary_function, 1000)) return;
                Log.e(TAG, "onClick: auxiliary_function");
                if (state != HUB_CONN_IN_CONNECTION) {
                    TTSSpeaker.speak(getString(R.string.no_connect_service), TTSSpeaker.HIGH);
                    return;
                }
                AuxiliaryFunctionActivity.listener = this;
                startActivity(new Intent(this, AuxiliaryFunctionActivity.class));


                break;
            case R.id.face_Recognition:
                if (checkReclick(R.id.face_Recognition, 1000)) return;
                Log.e(TAG, "onClick: face_Recognition");
                recognize("01", "人脸识别");
                break;
            case R.id.object_Recognition:
                if (checkReclick(R.id.object_Recognition, 1000)) return;
                Log.e(TAG, "onClick: object_Recognition");
                recognize("02", "物体识别");
                break;
            case R.id.money_Recognition:
                if (checkReclick(R.id.money_Recognition, 1000)) return;
                Log.e(TAG, "onClick: money_Recognition");
                recognize("03", "钱币识别");
                break;
            case R.id.scene:
                if (checkReclick(R.id.scene, 1000)) return;
                Log.e(TAG, "onClick: scene");
                recognize("04", "场景");
                break;
            case R.id.ocr:
                if (checkReclick(R.id.ocr, 1000)) return;
                Log.e(TAG, "onClick: ocr");
                recognize("05", "OCR");
                break;
            case R.id.navi:
                if (checkReclick(R.id.navi, 1000)) return;
                Log.e(TAG, "onClick: navi");
                AIBridge.getInstance().startListener(getString(R.string.where_go));
                break;
        }
    }

    private void metaPattern() {
        no_meta = !no_meta;
        Log.e(TAG, "metaPattern: " + no_meta);
        SharePreferenceUtils.setPrefBoolean(Constant.PRE_KEY_NO_META, no_meta);
        mNo_Meta.setChecked(no_meta);
        if (!mPresenter.checkUsb()) {
            setStateByUsb(mPresenter.checkUsb());
        }
    }

    private void recognize(String type, String showContent) {
//        （参数值 01 ：人脸   02:物体  03:钱币  04:场景   05:OCR ）
        if (mPresenter.state != HUB_CONN_IN_CONNECTION) {
            TTSSpeaker.speak(getString(R.string.no_connect_service), TTSSpeaker.HIGH);
            return;
        }
//        Toast.makeText(this, showContent, Toast.LENGTH_SHORT).show();
        JSONObject data = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            data.put("objectType", type);
            json.put("type", "recognize");
            json.put("data", data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HariServiceClient.getCommandEngine().sendData(json);
    }

    private void startBaseActivity(int type) {
        BaseActivity.type = type;
        Intent intent = new Intent(this, BaseActivity.class);
        intent.putExtra("type", type);
        startActivity(intent);
    }

    private void doFamilyHelp() {
        Intent intent = new Intent(this, FamilyHelpActivity.class);
        startActivity(intent);
    }

    private boolean checkPermission() {
        int checkRecordAudioPermission = ContextCompat.checkSelfPermission(this
                , Manifest.permission.RECORD_AUDIO);
        int checkWritePermission = ContextCompat.checkSelfPermission(this
                , Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int checkCamera = ContextCompat.checkSelfPermission(this
                , Manifest.permission.CAMERA);
        if (checkCamera != PackageManager.PERMISSION_GRANTED
                || checkRecordAudioPermission != PackageManager.PERMISSION_GRANTED
                || checkWritePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO
                            , Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    1);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ((MetaApplication) MetaApplication.mContext).init();
            checkMetaRom();
            mPresenter = new HubPresenter(this);
            initViews();
            mPresenter.init();
//            AIBridge.instance(this.getApplicationContext());
            boolean startCall = getIntent().getBooleanExtra("startCall", false);
            if (!startCall) {
                startCall = SharePreferenceUtils.getPrefBoolean(Constant.PRE_KEY_STARTCALL, false);
            }
            if (startCall) {
                if (no_meta != mNo_Meta.isChecked()) {
                    no_meta = !no_meta;
                    metaPattern();
                }
                if (!mPresenter.checkUsb() && !no_meta) {
                    metaPattern();
                }
                callCallee = SharePreferenceUtils.getPrefInt(Constant.PRE_KEY_CALLCALLEE, 0) == 0 ? CallEngine.getHARI_CALLEE_AI() : CallEngine.getHARI_CALLEE_HI();
                SharePreferenceUtils.setPrefBoolean(Constant.PRE_KEY_STARTCALL, false);
                mPresenter.doNext();
            }
            Log.d(TAG, "onRequestPermissionsResult init");
        } else {
            Log.e(TAG, "onRequestPermissionsResult error");
        }
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

    }

    @Override
    public void cutIn() {
        if (state == HUB_CONN_IN_CONNECTION || state == HUB_CONN_ON_CONNECTION) return;
        setStateByUsb(true);
//        Toast.makeText(this, R.string.meta_connected, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "cutIn --");
    }

    @Override
    public void cutOut() {
        Log.d(TAG, "cutOut --");
//        Toast.makeText(this, R.string.meta_disconnected, Toast.LENGTH_SHORT).show();
        if (state == HUB_CONN_IN_CONNECTION) return;
        if (state == HUB_CONN_ON_CONNECTION) {
            if (mPresenter != null)
                mPresenter.doNext();
            else {
                Log.e(TAG, "cutOut: mPresenter == null");
            }
        }
        setStateByUsb(false);
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy: ");
        super.onDestroy();
//        CustomAudioManager.getInstance(MetaApplication.mContext).changeToSpeaker();
        SelfStartUpBroadcast.removeDoubleClickHomeListener();
        unregisterReceiver(internetBroadcast);
        unregisterReceiver(batteryReceiver);
        if (HubActivity.class != null && !HubActivity.this.isFinishing())
            mPresenter.onDestroy();
//        unregisterReceiver(receiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN && mPresenter != null) {
//            callCallee=HARI_CALLEE_AI;
            return mPresenter.keyback();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode==101){
//            EasyPermissions.requestPermissions(this, "必要的权限", PERMISSIONREQUESTCODE, pers);
//        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        EasyPermissions.requestPermissions(this, "有些必要的权限未开启，不开启这些权限将会导致Meta不能正常工作，是否开启这些权限？", PERMISSIONREQUESTCODE, pers);
//        String result = "以下权限被拒绝请手动开启" + "\n";
//        for (String perm : perms) {
//            result += perm + "\n";
//        }
//        ToastUtil.show(this.getApplicationContext(), result);
    }

    @Override
    public void doubleClick() {

        Log.e(TAG, "doubleClick: " + no_meta + "  " + mPresenter.checkUsb());
        if (no_meta != SharePreferenceUtils.getPrefBoolean(Constant.PRE_KEY_NO_META, false)) {
            no_meta = SharePreferenceUtils.getPrefBoolean(Constant.PRE_KEY_NO_META, false);
            if (no_meta != mNo_Meta.isChecked()) {
                no_meta = !no_meta;
                metaPattern();
            }
        }
        if (mPresenter.checkUsb() || no_meta || MetaApplication.state == HUB_CONN_ON_CONNECTION || MetaApplication.state == HUB_CONN_IN_CONNECTION) {
            if (mCall.isEnabled()) {
                callCallee = CallEngine.getHARI_CALLEE_AI();
                if (mPresenter.getState() == HUB_CONN_ON_CONNECTION || mPresenter.getState() == HUB_CONN_IN_CONNECTION) {
                    Log.e(TAG, "doubleClick: 挂断");
                    setCallButtonEnable(false);
                } else {
                    Log.e(TAG, "doubleClick: 连接");
                    setCallButtonEnable(false);
                    sendHandlerMessage(CHANGECALLBUTTONENABLE, 1000);
                }
                callCallee = CallEngine.getHARI_CALLEE_AI();
                mPresenter.doNext();
            } else {
                Log.e(TAG, "doubleClick: 呼叫不可用");
            }
        } else {
            TTSSpeaker.speak(getString(R.string.conect_meta), TTSSpeaker.HIGH);
        }
    }
}
