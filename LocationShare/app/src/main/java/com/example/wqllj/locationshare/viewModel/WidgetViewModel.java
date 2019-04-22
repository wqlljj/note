package com.example.wqllj.locationshare.viewModel;

import android.content.Context;

import com.example.wqllj.locationshare.model.WidgetModel;

/**
 * Created by cloud on 2018/9/19.
 */

public class WidgetViewModel {
    private final WidgetModel widgetViewModel;

    public WidgetViewModel(Context context){
        widgetViewModel = new WidgetModel(context);
    }
    public void onDestory(){
        widgetViewModel.onDestory();
    }
}
