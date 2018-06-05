package com.cloudminds.meta.accesscontroltv.persenter;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.cloudminds.meta.accesscontroltv.bean.NewsBean;
import com.cloudminds.meta.accesscontroltv.bean.PersonInfoBean;
import com.cloudminds.meta.accesscontroltv.http.HCBaseHttp;
import com.cloudminds.meta.accesscontroltv.http.HttpClient;
import com.cloudminds.meta.accesscontroltv.model.MainModel;
import com.cloudminds.meta.accesscontroltv.util.SharePreferenceUtils;
import com.cloudminds.meta.accesscontroltv.view.InterFaceView;
import com.cloudminds.meta.accesscontroltv.view.MainActivity;
import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

import static com.cloudminds.meta.accesscontroltv.constant.Constant.MAIN_BG_KEY;

/**
 * Created by WQ on 2018/4/10.
 */

public class MainPersenter implements InterFacePersenter {
    private  InterFaceView view;
    private  MainModel mainModel;
    private String TAG="APP/MainPersenter";

    public MainPersenter(InterFaceView view) {
        this.view = view;
        mainModel = new MainModel(this);
    }
    Handler mHandler = new Handler()
    {
        public void handleMessage(Message paramAnonymousMessage)
        {
            super.handleMessage(paramAnonymousMessage);
            switch (paramAnonymousMessage.what){
                case 100:
                    MainPersenter.this.getAllNews("/screennews/getnews");
                    break;
            }

        }
    };
    public InterFaceView getView() {
        return view;
    }
    @Override
    public void onResponseSuccess(Object content) {
        String message = (String) content;
        Log.i(TAG,"get message:"+message);
        String list="";
        String category="";
        try {
            JSONObject jsonObject = new JSONObject(message);
            switch (jsonObject.getString("status")){
                case "show":
                    list = jsonObject.getJSONObject("list").toString();
                    category = jsonObject.getString("category");
                    if(!TextUtils.isEmpty(list)) {
                        try {
                            PersonInfoBean employeeBean = new Gson().fromJson(list, PersonInfoBean.class);
                            employeeBean.setCategory(category);
                            Log.e(TAG, "getMqttMessage: " + employeeBean);
                            ((MainActivity) view).showPersonInfo(employeeBean);
                        }catch (Exception e){
                            e.printStackTrace();
                            view.error("error:"+e.getMessage());
                        }
                    }else{
                        view.error("PersonInfo==null");
                    }
                    break;
                case "screen":
                    switch (jsonObject.getString("category")){
                        case "image":
                            String imagePath = jsonObject.getString("url");
                            if(!TextUtils.isEmpty(imagePath)) {
                                    SharePreferenceUtils.setPrefString(MAIN_BG_KEY,imagePath);
                                    ((MainActivity) view).changeMainBackground(imagePath);
                            }
                            String welcome = jsonObject.getString("welcome");
                            if(!TextUtils.isEmpty(welcome)) {
                                ((MainActivity) view).showWelcome(welcome);
                            }else{
                                ((MainActivity) view).scanVideo();
                            }
                            break;
                        case "video":
                            ((MainActivity) view).scanVideo();
                            break;
                    }
                    break;
                case "news":
                    if(jsonObject.getBoolean("update")){
                        list = jsonObject.getString("url");
                        getAllNews(list);
                    }
//                    ((MainActivity) view).showDynamics(strings);
                    break;
                default:
                    Log.e(TAG, "getMqttMessage: "+jsonObject.getString("status") );
                    Toast.makeText(((MainActivity) view), jsonObject.getString("list"), Toast.LENGTH_SHORT).show();
                    return;
            }

        } catch (JSONException e) {
            e.printStackTrace();
            view.error("error:"+e.getMessage());
        }

    }

    public void getAllNews(String list) {
        HttpClient.getAllNews(list, new HCBaseHttp.CallBack<NewsBean>() {
            @Override
            public void onResponse(NewsBean data) {
                Log.e(TAG, "onResponse: "+data );
                List<NewsBean.DataBean> data1 = data.getData();
                ((MainActivity) view).showDynamics(data1);
            }

            @Override
            public void onFailure(String msg) {
                ((MainActivity)MainPersenter.this.view).error("新闻获取失败");
                MainPersenter.this.mHandler.sendEmptyMessageDelayed(100, 5000L);
            }
        });
    }

    @Override
    public void onResponseFail(String msg) {
        view.error(msg);
    }

    public void destory(){
        mainModel.destory();
        view=null;
        mainModel=null;
    }
}
