package com.example.wangqi.mvvm.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import com.example.wangqi.mvvm.model.api.HCApiClient;
import com.example.wangqi.mvvm.model.bean.FaceResponse;
import com.example.wangqi.mvvm.model.http.HCBaseHttp;

import java.util.List;


/**
 * Created by wangqi on 2018/6/1.
 */

public class MainViewModel extends ViewModel {
    private MutableLiveData<String> name;
    private MutableLiveData<List<String>> names;
    private MutableLiveData<FaceResponse> weather;

    public MutableLiveData<String> getCurrentName() {
        if (name == null) {
            name = new MutableLiveData<>();
        }
        return name;
    }
    public void updateWeather(){
        HCApiClient.getFaceList(new HCBaseHttp.CallBack<FaceResponse>() {
            @Override
            public void onResponse(FaceResponse data) {
                weather.setValue(data);
            }

            @Override
            public void onFailure(String msg) {

            }
        });
    }
    public MutableLiveData<FaceResponse> getweather() {
        if (weather == null) {
            weather = new MutableLiveData<>();
        }
        return weather;
    }
    public MutableLiveData<List<String>> getNameList(){
        if (names == null) {
            names = new MutableLiveData<>();
        }
        return names;
    }

}
