package com.cloudminds.meta.activity;

/**
 * Created by tiger on 17-4-1.
 */

public interface IActivateView {

    String getUser();
    String getPass();

    void setButtonEnable(boolean enable);
    void nextSetup();
    void setStateByUsb(boolean state);
    void showDialog(String code,String message);

}
