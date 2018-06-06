package com.cloudminds.meta.fragment;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.cloudminds.hc.metalib.utils.ToastUtil;
import com.cloudminds.meta.R;
import com.cloudminds.meta.activity.IHubView;
import com.cloudminds.meta.adapter.RecordsRVAdapter;
import com.cloudminds.meta.application.MetaApplication;
import com.cloudminds.meta.bean.ChatMessage;
import com.cloudminds.meta.constant.Constant;
import com.cloudminds.meta.service.HubServiceConnector;
import com.cloudminds.meta.service.navigation.OutdoorNavigator;
import com.cloudminds.meta.util.DateUtil;

import java.util.ArrayList;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import static com.cloudminds.meta.application.MetaApplication.mContext;


/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class RecordsFragment extends Fragment implements View.OnClickListener ,MetaApplication.NotifyItemListener,IHubView, Callback {

    private  RecordsFragment fragment;
    private EditText msg;
    private HubServiceConnector hubServiceConnector;
    private static String TAG="META/RecordsFragment";
    private   RecyclerView recyclerView;
    private  RecordsRVAdapter recordsRVAdapter;
    private static   ArrayList<ChatMessage> messages;
    private CheckBox start_recognition;
    private Toast logToast;
    private CheckBox auto_recognition;


    public RecordsFragment() {
        RecordsFragment.messages = Constant.msg;
        fragment=this;
    }
//    public static RecordsFragment newInstance() {
////        if(fragment==null) {
//            Log.e(TAG, "newInstance: " );
//            fragment = new RecordsFragment();
////        }
//        return fragment;
//    }
    @Override
    public void onAttach(Activity activity) {
        Log.e(TAG, "onAttach: " );
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        Log.e(TAG, "onDetach: " );
        super.onDetach();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        Log.e(TAG, "onDestroyView: " );
        super.onDestroyView();
        MetaApplication.removeNotifyItemListener(fragment);
        recordsRVAdapter=null;
        recyclerView=null;
        fragment=null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView: " );
        hubServiceConnector = HubServiceConnector.getIntance(getActivity().getApplicationContext());
        View view = inflater.inflate(R.layout.fragment_records, container, false);
        view.findViewById(R.id.clear).setOnClickListener(this);
//        MetaApplication.setState_listenr(this);
        msg = ((EditText) view.findViewById(R.id.msg));
//        auto_recognition = ((CheckBox) view.findViewById(R.id.btn_auto_recognition));
//        auto_recognition.setOnClickListener(this);
//        start_recognition = ((CheckBox) view.findViewById(R.id.btn_start_recognition));
//        start_recognition.setChecked(MetaApplication.isRecognition());
//        start_recognition.setOnClickListener(this);
//        if(!MetaApplication.isRecognition()) {
//            MetaApplication.setIsAutoRecognition(false);
//            auto_recognition.setEnabled(false);
//        }
//        auto_recognition.setChecked(MetaApplication.isAutoRecognition());
        recyclerView = ((RecyclerView) view.findViewById(R.id.recyclerview));
        view.findViewById(R.id.send_msg).setOnClickListener(this);
        recordsRVAdapter = new RecordsRVAdapter(this.getActivity(),messages,new int[]{R.layout.chat_left_item,R.layout.chat_right_item});
        recyclerView.setAdapter(recordsRVAdapter);
        MetaApplication.addNotifyItemListener(fragment);
        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                Log.e(TAG, "onLayoutChange: " );
                if(messages.size()!=0)
                recyclerView.smoothScrollToPosition(messages.size()-1);
            }
        });
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.send_msg:
                String words = msg.getText().toString().trim();
                if(TextUtils.isEmpty(words)){
//                    OutdoorNavigator.getInstance().onGetNavigationText(0,"前方左转");
                    ToastUtil.show(getContext().getApplicationContext(), R.string.content_null);
                }else{
//                    if("结束导航".contains(words)){
//                        IndoorNavigator.sendStopNavi();
//                        messages.add(new ChatMessage(ChatMessage.Type.CHAT_RGIHT, DateUtil.getTodayDateTimeStr(),words));
//                        recordsRVAdapter.notifyItemInserted(messages.size()-1);
//                        recyclerView.smoothScrollToPosition(messages.size()-1);
//                        msg.setText("");
//                        Log.e(TAG, "sendMsg: "+words);
//                        break;
//                    }
                    hubServiceConnector.sendMessage(words);
                    messages.add(new ChatMessage(ChatMessage.Type.CHAT_RGIHT, DateUtil.getTodayDateTimeStr(),words));
                    recordsRVAdapter.notifyItemInserted(messages.size()-1);
                    recyclerView.smoothScrollToPosition(messages.size()-1);
                    msg.setText("");
                    Log.e(TAG, "sendMsg: "+words);
                }
                break;
            case R.id.clear:
//                OutdoorNavigator.getInstance().onGetNavigationText(0,"前方100米左转");
                RecordsFragment.messages.clear();
                recordsRVAdapter.notifyDataSetChanged();
                break;
//            case R.id.btn_start_recognition:
//                try {
//                    if(MetaApplication.state==Constant.HUB_CONN_IN_CONNECTION) {
////                        HCApiClient.recognition(PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_SERVER_ADDRESS, SERVER_ADDRESS),
////                                "8891",
////                                PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_ACCOUNT,"2919276686923065298"),
////                                start_recognition.isChecked(),this);
//                        HariServiceClient.getCommandEngine().sendAutoRecognize(start_recognition.isChecked());
//
//                        MetaApplication.setIsRecognition(start_recognition.isChecked());
//                        if(start_recognition.isChecked()){
//                            auto_recognition.setEnabled(true);
//                        }else{
//                            auto_recognition.setEnabled(false);
//                            auto_recognition.setChecked(false);
//                            MetaApplication.setIsAutoRecognition(false);
//                        }
//                        logAndToast(getString(R.string.object_recognition)+(start_recognition.isChecked()?getString(R.string.open):getString(R.string.close)));
//                    }else{
//                        logAndToast(getString(R.string.no_video_no_recognition));
//                        start_recognition.setChecked(true);
//                        auto_recognition.setEnabled(true);
//                        MetaApplication.setIsRecognition(start_recognition.isChecked());
//                    }
//                }catch (Exception e){
//                    e.printStackTrace();
//                    start_recognition.setChecked(true);
//                    auto_recognition.setEnabled(true);
//                    MetaApplication.setIsRecognition(start_recognition.isChecked());
//                }
//                break;
//            case R.id.btn_auto_recognition:
//                try {
//                    if(MetaApplication.state==Constant.HUB_CONN_IN_CONNECTION) {
////                        HCApiClient.recognition(PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_SERVER_ADDRESS, SERVER_ADDRESS),
////                                "8891",
////                                PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_ACCOUNT,"2919276686923065298"),
////                                auto_recognition.isChecked(),this);
//                        MetaApplication.setIsAutoRecognition(auto_recognition.isChecked());
//                        logAndToast(getString(R.string.auto_recognition)+(auto_recognition.isChecked()?getString(R.string.open):getString(R.string.close)));
//                    }else{
//                        logAndToast(getString(R.string.no_video_no_recognition));
//                        auto_recognition.setEnabled(false);
//                        MetaApplication.setIsAutoRecognition(auto_recognition.isChecked());
//                    }
//                }catch (Exception e){
//                    e.printStackTrace();
//                    auto_recognition.setEnabled(false);
//                    MetaApplication.setIsAutoRecognition(auto_recognition.isChecked());
//                }
//                break;
        }
    }
    @Override
    public void notifyItem() {
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                recordsRVAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messages.size()-1);
            }
        });
    }
    /**
     * Log |msg| and Toast about it.
     */
    private void logAndToast(String msg) {
        Log.d(TAG, msg);
        ToastUtil.show(mContext, msg);
    }

    @Override
    public void setStateByUsb(boolean conn) {
    }
    @Override
    public void setStateByCurrentMeta(boolean current) {
    }
    @Override
    public void setUIState(int state, String message) {
//        switch (state){
//            case Constant.HUB_CONN_IN_CONNECTION:
////                break;
//            case Constant.HUB_CONN_DISCONNECT:
//            case Constant.HUB_CONN_ON_CONNECTION:
//            case Constant.HUB_CONN_NOMAL:
//            case Constant.HUB_CONN_END:
//            case Constant.CALL_FAILED:
//                if(!start_recognition.isChecked()) {
//                    start_recognition.setChecked(true);
//                    auto_recognition.setEnabled(true);
//                    auto_recognition.setChecked(false);
//                    HCApiClient.recognition(PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_SERVER_ADDRESS, SERVER_ADDRESS),
//                            "8891",PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_ACCOUNT,"2919276686923065298")
//                            ,start_recognition.isChecked(),this);
////                    HCApiClient.recognition(PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_SERVER_ADDRESS, SERVER_ADDRESS),
////                            "8891",PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_ACCOUNT,"2919276686923065298")
////                            ,start_recognition.isChecked(),this);
//                    MetaApplication.setIsRecognition(start_recognition.isChecked());
//                    MetaApplication.setIsAutoRecognition(auto_recognition.isChecked());
//                }else if(auto_recognition.isChecked()){
//                    auto_recognition.setEnabled(true);
//                    auto_recognition.setChecked(false);
////                    HCApiClient.recognition(PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_SERVER_ADDRESS, SERVER_ADDRESS),
////                            "8891",PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_ACCOUNT,"2919276686923065298")
////                            ,start_recognition.isChecked(),this);
//                    MetaApplication.setIsAutoRecognition(auto_recognition.isChecked());
//                }
//                break;
//        }
    }


    @Override
    public void onResponse(Call call, Response response) {
        logAndToast("成功");
    }

    @Override
    public void onFailure(Call call, Throwable t) {
            logAndToast(t.getMessage());
    }
}
