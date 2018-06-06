package com.cloudminds.hc.hariservice.call.listener;

/**
 * Created by zoey on 17/4/17.
 */

public interface CallEventListener {

    public void onCallConnecting();

    public void onMessageChannelConnected();

    public void onMediaChannelConnected();

    public void onMediaChannelDisconnected(final String code, final String message);

    public void onCallError(final String code, final String message);

    public void onCallRestart();

    public void onCallClosed();

    public void onCallException(final String code);

}
