package com.cloudminds.meta.manager;

import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by WQ on 2018/3/9.
 */

public class ConflictEventManager {
    public final static String CALLING_EVENT="calling";
    public final static String FAMILYMANAGE_EVENT="familymanage";
    //冲突时间记录
    private final static HashMap<String,EventChecker> existingEvents =new HashMap<>();
    private final static HashMap<String,HashSet<String>> conflictRelation=
            new HashMap<>();
    static {
        //呼叫冲突
        HashSet<String> hashSet=new HashSet<>();
        hashSet.add(FAMILYMANAGE_EVENT);
        conflictRelation.put(CALLING_EVENT,hashSet);
        //亲友管理冲突
        hashSet=new HashSet<>();
        hashSet.add(CALLING_EVENT);
        conflictRelation.put(FAMILYMANAGE_EVENT,hashSet);
    }

    private static String TAG="ConflictEventManager";

    public static String addEvent(String event,EventChecker checker) throws Exception {
        if(!conflictRelation.containsKey(event)){
            throw new Exception("该事件未定义："+event);
        }
        Log.e(TAG, "addEvent: "+event );
        HashSet<String> hashSet = conflictRelation.get(event);
        if(hashSet.size()>0){
            for (String s : hashSet) {
                if(existingEvents.containsKey(s)&&existingEvents.get(s).check()){
                    return s;
                }
            }
        }
        existingEvents.put(event,checker);
        return "";
    }
    public static void removeEvent(String event){
        Log.e(TAG, "removeEvent: " );
        if(existingEvents.containsKey(event)){
            Log.e(TAG, "removeEvent: "+event );
            existingEvents.remove(event);
        }
    }

    public interface EventChecker{
        boolean check();
    }
}
