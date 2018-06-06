package com.cloudminds.meta.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

public class LocaleChangeReceiver extends BroadcastReceiver {
    public static String language="en";//en 英文 zh 中文
    private String TAG="META/LocaleChangeRece";
    public static HashMap<String,LocaleChangeListener> listeners;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "mReceiver  onReceive  intent.getAction(): "+intent.getAction());

        if(intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
            String language = getLanguage(context);
            Log.e(TAG,"Language change language = "+language);
            if(!language.equals(LocaleChangeReceiver.language)){
                LocaleChangeReceiver.language=language;
                Set<String> keySet = listeners.keySet();
                for (String key : keySet) {
                    listeners.get(key).onChange(language);
                }
            }
        }
    }
    public static void addListener(LocaleChangeListener listener){
        if(listeners==null){
            listeners=new HashMap<>();
        }
        listeners.put(listener.getClass().getSimpleName(),listener);
    }
    public static void removeListener(LocaleChangeListener listener){
        if(listeners.containsKey(listener.getClass().getSimpleName())){
            listeners.remove(listener.getClass().getSimpleName());
        }
        if(listeners.size()==0){
            listeners=null;
        }
    }
    public static void clear(){
        if(listeners!=null){
            listeners.clear();
            listeners=null;
        }
    }


    public static String getLanguage(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        return locale.getLanguage();
    }
    public  interface  LocaleChangeListener{
        void onChange(String language);
    }
}
