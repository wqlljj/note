// IHubService.aidl
package com.cloudminds.meta.aidl;

import com.cloudminds.meta.aidl.IHubReceiver;

interface IHubService {

    void setCallStateListener(IHubReceiver receiver);
    void callStart(int type);
    void callStop();
    void sendMessage( String msg);
    void sendData(String data);
}
