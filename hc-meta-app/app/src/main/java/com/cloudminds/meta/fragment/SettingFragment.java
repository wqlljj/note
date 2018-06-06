package com.cloudminds.meta.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.baidu.tts.tools.SharedPreferencesUtils;
import com.cloudminds.hc.cloudService.api.HCApiClient;
import com.cloudminds.hc.hariservice.HariServiceClient;
import com.cloudminds.hc.hariservice.call.CallEngine;
import com.cloudminds.hc.hariservice.command.CommandEngine;
import com.cloudminds.hc.hariservice.utils.BaseConstants;
import com.cloudminds.hc.hariservice.utils.PreferenceUtils;
import com.cloudminds.hc.metalib.HCMetaUtils;
import com.cloudminds.hc.metalib.utils.ToastUtil;
import com.cloudminds.meta.R;
import com.cloudminds.meta.application.MetaApplication;
import com.cloudminds.meta.constant.Constant;

import org.json.JSONObject;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.cloudminds.hc.hariservice.utils.BaseConstants.SERVER_ADDRESS;

public class SettingFragment extends Fragment implements OnClickListener, Callback {
	private View view;
	private Activity activity;
	private String serverport;
	private int lowpawer;
	private int lowhealth;
	private EditText et_lp;
	private EditText et_spt;
	private EditText et_lh;
	private Button save;
	private EditText et_hsi;
	private EditText et_hsp;
	private EditText et_hsCustomer;
	private EditText et_hsvw;
	private EditText et_hsvh;
	private int videoWidth;
	private int videoHeight;
	private EditText et_hsUserName;
	private EditText et_hsRobot;
	private EditText et_hsBitrate;
	private Spinner et_hsFrameRate;
	private EditText et_hsTenant;
	private TextView version;
	private ToggleButton tb_gps;
	private boolean isGPSEnabled;
	private CommandEngine cmdEngine;
	private EditText et_hsPassWord;
	private ToggleButton tb_start_recognition;
	private boolean isRecognitionEnable;
	private String TAG="META/SettingFragment";


	@Override
	public void onAttach(Activity activity) {
		Log.e(TAG, "onAttach: " );
		super.onAttach(activity);
		this.activity = activity;
	}
	@Override
	public void onDetach() {
		Log.e(TAG, "onDetach: " );
		super.onDetach();
		activity=null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onDestroy() {
		Log.e(TAG, "onDestroy: " );
		super.onDestroy();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.e(TAG, "onCreateView: " );
		view = inflater.inflate(R.layout.fragment_setting, container, false);
		 cmdEngine = HariServiceClient.getCommandEngine();
		lowpawer = HCMetaUtils.getLowPawer();
		lowhealth =HCMetaUtils.getLowHealth();
		serverport=HCApiClient.getServicePort();
		version = ((TextView) view.findViewById(R.id.version));
		et_spt = ((EditText) view.findViewById(R.id.server_port));
		et_spt.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				if(et_spt.getCurrentTextColor()==getResources().getColor(R.color.red)){
					et_spt.setTextColor(getResources().getColor(R.color.black));
				}
			}
		});
		et_lp = ((EditText) view.findViewById(R.id.low_Pawer));
		et_lh = ((EditText) view.findViewById(R.id.low_Health));
		et_hsi = ((EditText) view.findViewById(R.id.hariServer_ip));
		et_hsi.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				if(et_hsi.getCurrentTextColor()==getResources().getColor(R.color.red)){
					et_hsi.setTextColor(getResources().getColor(R.color.black));
				}
			}
		});
		et_hsp = ((EditText) view.findViewById(R.id.hariServer_port));
		et_hsp.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				if(et_hsp.getCurrentTextColor()==getResources().getColor(R.color.red)){
					et_hsp.setTextColor(getResources().getColor(R.color.black));
				}
			}
		});
		et_hsvw = ((EditText) view.findViewById(R.id.hariServer_videoWidth));
		et_hsvh = ((EditText) view.findViewById(R.id.hariServer_videoHeight));
		et_hsCustomer = ((EditText) view.findViewById(R.id.hariServer_customer));
		et_hsUserName = ((EditText) view.findViewById(R.id.hariService_username));
		et_hsPassWord = ((EditText) view.findViewById(R.id.hariService_password));
		et_hsRobot = ((EditText) view.findViewById(R.id.hariService_robot));
		et_hsBitrate =  ((EditText) view.findViewById(R.id.hariService_bitrate));
		et_hsFrameRate = ((Spinner) view.findViewById(R.id.hariService_framerate));


		et_hsTenant = ((EditText) view.findViewById(R.id.hariService_tenantId));
		save = ((Button) view.findViewById(R.id.save));
		et_spt.setText(serverport);
		et_hsCustomer.setText(PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_CUSTOMER,"hi@cloudminds.com"));
		et_hsUserName.setText(PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_ACCOUNT,"2919276686923065298"));
		et_hsPassWord.setText(PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_PASSWORD,"123456"));
		CallEngine callEngine = HariServiceClient.getCallEngine();
		if(TextUtils.isEmpty(PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_SERVER_ADDRESS,""))) {
			callEngine.setServer("10.11.32.173");
			callEngine.setPort("7443");
		}
		et_hsi.setText(PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_SERVER_ADDRESS, ""));
		et_hsp.setText(PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_SERVER_PORT, ""));
		et_hsvw.setText(callEngine.getParam(CallEngine.PARAM_VIDEO_WIDTH));
		et_hsvh.setText(callEngine.getParam(CallEngine.PARAM_VIDEO_HEIGHT));
		et_lp.setText(""+lowpawer);
		et_lh.setText(""+lowhealth);
		et_hsRobot.setText(HariServiceClient.getCallEngine().getRobotType());
		save.setOnClickListener(this);
		version.setText(getActivity().getResources().getString(R.string.app_name)+" V"+getVersion(getContext()));
        //带宽码率
//		et_hsBitrate.setText(callEngine.getParam(CallEngine.PARAM_BIT_RATE));
		et_hsBitrate.setText(callEngine.getParam(CallEngine.PARAM_VIDEO_BPS));
		//视频帧率
		String[] array = getResources().getStringArray(R.array.frame_rates);
		String frame_rate = callEngine.getParam(CallEngine.PARAM_VIDEO_FPS);
		int index=0;
		for (int i = 0; i < array.length; i++) {
			if(array[i].equals(frame_rate)){
				index=i;
				break;
			}
		}
		Log.e(TAG, "onCreateView: "+ Arrays.toString(array)+"\r\n"+"frame_rate = "+frame_rate+"\r\n"+"index = "+index );
		et_hsFrameRate.setSelection(index);
//		et_hsFrameRate.setText(callEngine.getParam(CallEngine.PARAM_VIDEO_FPS));
		//租户id
		et_hsTenant.setText(callEngine.getTenantId());

		isGPSEnabled = PreferenceUtils.getPrefBoolean("GPSEnable",true);
		tb_gps = (ToggleButton) view.findViewById(R.id.status_gps);
		tb_gps.setChecked(isGPSEnabled);
		tb_gps.setOnClickListener(this);

		isRecognitionEnable = PreferenceUtils.getPrefBoolean("RecognitionEnable",false);
		tb_start_recognition = (ToggleButton) view.findViewById(R.id.btn_start_recognition);
		tb_start_recognition.setChecked(isRecognitionEnable);
		tb_start_recognition.setOnClickListener(this);

        return view;
	}
	public static String getVersion(Context mContext) {
		try {
			PackageManager manager = mContext.getPackageManager();
			PackageInfo info = manager.getPackageInfo(mContext.getPackageName(), 0);
			String version = info.versionCode+"\r\n"+info.versionName;
			return version;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@IdRes
	int btId;
	long clickTime=0l;
	private boolean checkReclick(@IdRes int id,long timeLimit) {
		Log.e(TAG, "checkReclick: "+id+"  "+timeLimit );
		if(btId==id&& System.currentTimeMillis()-clickTime<timeLimit){
			ToastUtil.show(getActivity().getApplicationContext(), R.string.repetitive_operation);
			Log.e(TAG, "checkReclick: true" );
			return true;
		}
		Log.e(TAG, "checkReclick: false" );
		btId=id;
		clickTime=System.currentTimeMillis();
		return false;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()){
			case R.id.status_gps:
			{
				if (checkReclick(view.getId(),500)) return;
				isGPSEnabled = !isGPSEnabled;
				tb_gps.setChecked(isGPSEnabled);
				PreferenceUtils.setPrefBoolean("GPSEnable",isGPSEnabled);
/*
				Bitmap image = null;
				AssetManager am = getResources().getAssets();
				try
				{
					InputStream is = am.open("pic-test2.png");
					image = BitmapFactory.decodeStream(is);
					is.close();

					ByteArrayOutputStream out = new ByteArrayOutputStream();
					image.compress(Bitmap.CompressFormat.PNG, 60, out);

					byte[] picBytes = out.toByteArray();

					InputStream is2 = am.open("pic-test.jpg");
					image = BitmapFactory.decodeStream(is2);
					is2.close();

					ByteArrayOutputStream out2 = new ByteArrayOutputStream();
					image.compress(Bitmap.CompressFormat.PNG, 60, out2);
					byte[] picBytes2 = out2.toByteArray();


					List<byte[]> picList = new ArrayList<>();
                    //picList.add(picBytes);
					picList.add(picBytes2);
					HariServiceClient.getCommandEngine().sendPictureList(picList);
				}

				catch (IOException e)
				{
					e.printStackTrace();
				}
				*/
			}

				break;
			case R.id.btn_start_recognition:
				if (checkReclick(view.getId(),500)) return;
				isRecognitionEnable = !isRecognitionEnable;
				tb_start_recognition.setChecked(isRecognitionEnable);
				PreferenceUtils.setPrefBoolean("RecognitionEnable",isRecognitionEnable);
				try {
					if(MetaApplication.state== Constant.HUB_CONN_IN_CONNECTION) {
						MetaApplication.setIsRecognition(tb_start_recognition.isChecked());
//						HariServiceClient.getCommandEngine().sendAutoRecognize(PreferenceUtils.getPrefBoolean("RecognitionEnable",false));
						JSONObject data = new JSONObject();
						try {
							data.put("state",PreferenceUtils.getPrefBoolean("RecognitionEnable",false));
						}catch (Exception e){
							e.printStackTrace();
						}
						JSONObject json = new JSONObject();
						try {
							json.put("type","autoRecognize");
							json.put("data",data);
						}catch (Exception e){
							e.printStackTrace();
						}
						HariServiceClient.getCommandEngine().sendData(json);
					}
					}catch (Exception e){
					e.printStackTrace();
				}
				break;
			case R.id.save:
				if (checkReclick(view.getId(),2000)){
					Log.e(TAG, "onClick: " );
					return;
				}
				String regexPath="(\\d|[1-9]\\d|1\\d{2}|2[0-5][0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-5][0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-5][0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[1-5])";
				String regexPort="([0-9]|[1-9]\\d{1,3}|[1-5]\\d{4}|6[0-5]{2}[0-3][0-5])";
				boolean isSuccess=true;
//				if(!serverpath.matches(regexPath)){
//					et_sph.setTextColor(getResources().getColor(R.color.red));
//					isSuccess=false;
//				}else{
//					HCApiClient.setServiceAddress(serverpath);
//				}
				serverport=et_spt.getText().toString();
				if(!serverport.matches(regexPort)){
					et_spt.setTextColor(getResources().getColor(R.color.red));
					isSuccess=false;
				}else{
					HCApiClient.setServicePort(serverport);
				}
				lowpawer=Integer.valueOf(et_lp.getText().toString());
				lowhealth=Integer.valueOf(et_lh.getText().toString());
				videoWidth=Integer.valueOf(et_hsvw.getText().toString());
				videoHeight=Integer.valueOf(et_hsvh.getText().toString());

				HCMetaUtils.setLowPawer(lowpawer);
				HCMetaUtils.setLowHealth(lowhealth);
				CallEngine callEngine = HariServiceClient.getCallEngine();
				callEngine.setParam(CallEngine.PARAM_VIDEO_WIDTH,et_hsvw.getText().toString());
				callEngine.setParam(CallEngine.PARAM_VIDEO_HEIGHT,et_hsvh.getText().toString());

				String port = et_hsi.getText().toString();
				String[] split = port.split("\\.");
				if(split.length!=4||!port.matches("^[0-9].*[0-9]$")){
					et_hsi.setTextColor(getResources().getColor(R.color.red));
					isSuccess=false;
				}else{
					try {
						int a = Integer.valueOf(split[0]);
						int b = Integer.valueOf(split[1]);
						int c = Integer.valueOf(split[2]);
						int d = Integer.valueOf(split[3]);
						if(a<=0||a>255||b<=0||b>255||c<=0||c>255||d<=0||d>255){
							et_hsi.setTextColor(getResources().getColor(R.color.red));
							isSuccess=false;
						}else {
							callEngine.setServer(et_hsi.getText().toString());
							HCApiClient.setServiceAddress(et_hsi.getText().toString());
						}
					}catch (NumberFormatException e){
						et_hsi.setTextColor(getResources().getColor(R.color.red));
						isSuccess=false;
						Log.e(TAG, "onClick: 包含非数字或.的字符" );
					}

				}
				if(!et_hsp.getText().toString().matches(regexPort)){
					et_hsp.setTextColor(getResources().getColor(R.color.red));
					isSuccess=false;
				}else{
					callEngine.setPort(et_hsp.getText().toString());
				}
				if(!isSuccess){
					ToastUtil.show(getContext(), R.string.save_fail);
					return;
				}
				callEngine.setCustomer(et_hsCustomer.getText().toString());
				callEngine.setAccount(et_hsUserName.getText().toString());
				callEngine.setPassword(et_hsPassWord.getText().toString());
				callEngine.setRobotType(et_hsRobot.getText().toString());
				callEngine.setTenantId(et_hsTenant.getText().toString());
				Log.e(TAG, "onClick: 帧率 "+et_hsFrameRate.getSelectedItem().toString() );
				callEngine.setParam(CallEngine.PARAM_VIDEO_FPS,et_hsFrameRate.getSelectedItem().toString());
				callEngine.setParam(CallEngine.PARAM_VIDEO_BPS,et_hsBitrate.getText().toString());
				ToastUtil.show(activity,R.string.save_success);
				break;
		}
	}



	@Override
	public void onResponse(Call call, Response response) {
//		logAndToast("成功");
	}

	@Override
	public void onFailure(Call call, Throwable t) {
//		logAndToast("失败");
	}
}
