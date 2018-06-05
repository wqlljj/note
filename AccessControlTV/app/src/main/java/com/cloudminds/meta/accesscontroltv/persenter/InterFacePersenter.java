package com.cloudminds.meta.accesscontroltv.persenter;

/**
 * Created by WQ on 2018/4/10.
 */

public interface InterFacePersenter {
    void onResponseSuccess(Object content);
    void onResponseFail(String msg);
    void destory();
}
