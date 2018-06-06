package com.cloudminds.hc.hariservice.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by SX on 2018/1/29.
 */

public class ThreadPoolUtils {
    private static ExecutorService executorService;

    public static void init(){
        executorService = Executors.newCachedThreadPool();
    }
    public static void execute(Runnable runnable){
        executorService.execute(runnable);
    }
    public static void destory(){
        if(executorService!=null){
            if(!executorService.isShutdown())
                executorService.shutdownNow();
            executorService=null;
        }
    }
}
