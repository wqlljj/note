/*
 *  Copyright 2013 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.cloudminds.hc.hariservice.webrtc;

import com.cloudminds.hc.hariservice.utils.BaseConstants;
import com.cloudminds.hc.hariservice.utils.PreferenceUtils;

import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

/**
 * AppRTCClient is the interface representing an AppRTC client.
 */
public interface AppRTCClient {
    /**
     * Struct holding the connection parameters with Assistant Server.
     */
    public static class AssistantConnectionParameters {
        public String wssUrl;
        public final String username;
        public boolean initiator;
        public String participantName;
        public final String password;
        public final String robotType;
        public final String customer;
        public final String rcuID;
        public int destroyMedia;

        public AssistantConnectionParameters(String wssUrl, String username, String pwd, String robot) {
            this.wssUrl = wssUrl;
            this.username = username;
            this.initiator = false;
            this.participantName = null;
            this.password = pwd;
            this.robotType = robot;
            this.customer = "hi@cloudminds.com";
            this.rcuID = "2919276686923065298";
            this.destroyMedia = 0;
        }

        public AssistantConnectionParameters() {
            String server = PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_SERVER_ADDRESS, BaseConstants.SERVER_ADDRESS);
            String port = PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_SERVER_PORT,BaseConstants.SERVER_PORT);

            this.wssUrl = "ws://"+ server+":"+port+"/hari";
            this.username = PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_ACCOUNT,"2919276686923065298");
            this.initiator = false;
            this.participantName = null;
            this.password = PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_PASSWORD,"123456");
            this.robotType = PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_ROBOT_TYPE,"meta2");
            this.customer = PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_CUSTOMER,"hi@cloudminds.com");
            this.rcuID = PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_RCUID,"2919276686923065298");
            this.destroyMedia = 0;
        }

    }

    /**
     * Connect to Assistant Server using given connection parameters.
     * Once connection is established onSignalServerConnected callback
     * is invoked.
     */
    public void connectSignalServer(AssistantConnectionParameters connectionParameters,final String sessionId);

    public void rebindSignalServer(final String sessionId);

    /**
     * Once heartbeat timeout , close current connection, reconnect
     */

    public void reconnectSignalServer(AssistantConnectionParameters connectionParameters,final String sessionId);

    /**
     * Send offer SDP to the other participant.
     */
    public void sendCallStart(final String sessionId, final SessionDescription sdp);

    /**
     * Send answer SDP to the other participant.
     */
    //public void sendAnswerSdp(final SessionDescription sdp);

    /**
     * Send Ice candidate to the other participant.
     */
    public void sendCallUpdate(final String sessionId, final IceCandidate candidate);

    public void sendHeartbeat(final String sessionId);

    public void sendCallStop(final String sessionId);

    public void logout(final String sessionId);

    /**
     * Disconnect from Assistant Server.
     */
    public void disconnectFromSignalServer();
    
    public void sendLocation(final String sessionId);

    public void sendInfo(final String sessionId, JSONObject infoData);

    public void sendHariInfo(final String sessionId, final String infoData);

    public void responseConnect(final String sessionId);

    public void getStats();

    public boolean isWsConnected();
    /**
     * Callback interface for messages delivered on signaling channel.
     *
     * <p>Methods are guaranteed to be invoked on the UI thread of |activity|.
     */
    public static interface SignalingEvents {

        /**
         * Callback fired once connected to Assistant Server.
         */
        public void onSignalServerConnected(final String sessionId);

        /**
         * Callback fired once call session is allocated by server.
         */
        public void onCallSessionCreated(final String sessionId);

        /**
         * Callback fired once remote SDP is received.
         */
        public void onCallAccepted(/*final String sessionId,*/ final SessionDescription sdp);

        /**
         * Callback fired once remote Ice candidate is received.
         */
        public void onCallUpdate(final String sessionId, final IceCandidate candidate);

        /**
         * Callback fired once received heartbeatResponse from remote
         * @param sessionId
         */
        public void onHeartbeatResponse(final String sessionId);

        /**
         * Callback fired once remote Ice candidate is received.
         */
        public void onCallStop(final String sessionId,String detail);

        /**
         * Callback fired once object-identified message is received.
         */
        public void onObjectIdentified(final String objectName);

        /**
         * Callback fired once face-identified message is received.
         */
        public void onFaceIdentified(final String faceName);
        /**
         * Callback fired once navigation command is received.
         */
        public void onCommandReceived(final String command);

        public void onInfo(final String type, final String data);
        
        /***
         * Callback fired once navigation start is received
         */
        public void onSlamNavigationStart(final String startPoint ,final String FinishPoint);

        /**
         * Callback fired once channel is closed.
         */
        public void onChannelClose();

        /**
         * Callback fired once channel connection error happened.
         */
        public void onChannelConnectionError(final String description);

        /**
         * Callback fired once channel authentication error happened.
         */
        public void onChannelAuthenticationError(final String description);

        /**
         * Callback fired once channel message invalid error happened.
         */
        public void onChannelSignalError(final String description);

        /**
         * Callback fired once channel call rejected error happened.
         */
        public void onChannelCallRejected(final String description);

        public void onInfo(final JSONObject data);

    }
}
