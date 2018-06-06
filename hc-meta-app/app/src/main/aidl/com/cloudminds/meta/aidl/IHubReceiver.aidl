// IHubReceiver.aidl
package com.cloudminds.meta.aidl;

// Declare any non-default types here with import statements

interface IHubReceiver {

    void callResult(int state,String message);
}
