package com.cloudminds.meta.activity;

/**
 * Created by tiger on 17-4-10.
 */

public interface IHubView {

    void setStateByUsb(boolean conn);
    void setStateByCurrentMeta(boolean current);
    void setUIState(int state,String message);
}
