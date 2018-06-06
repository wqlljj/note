/*
 *  Copyright 2014 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.cloudminds.hc.hariservice.webrtc;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import com.cloudminds.hc.hariservice.utils.BaseConstants;
import com.cloudminds.hc.hariservice.utils.Log.LogUtils;
import com.cloudminds.hc.hariservice.utils.PreferenceUtils;
import com.cloudminds.hc.hariservice.webrtc.WebSocketChannelClient.WebSocketChannelEvents;
/**
 * Negotiates signaling for the communication with Assistant Server.
 */
public class WebSocketRTCClient implements AppRTCClient, WebSocketChannelEvents {
    private static final String TAG = "HS/WSRTCClient";

    private enum ConnectionState {
        NEW, CONNECTING, CONNECTED, CLOSED, ERROR
    };

    private final LooperExecutor executor;
    private SignalingEvents events;
    private WebSocketChannelClient wsClient;
    private ConnectionState wsState;
    private AssistantConnectionParameters connectionParameters;

    public WebSocketRTCClient(SignalingEvents events, LooperExecutor executor) {
        this.events = events;
        this.executor = executor;
        wsState = ConnectionState.NEW;
        executor.requestStart();
    }

    /**
     * Connect to Assistant Server using given connection parameters.
     */
    @Override
    public void connectSignalServer(AssistantConnectionParameters connectionParameters, final String sessionId) {
        this.connectionParameters = connectionParameters;
        Log.d(TAG, "To call connectToAssistantServerInternal asynchronously");
        executor.execute(new Runnable() {
            @Override
            public void run() {
                connectToAssistantServerInternal(sessionId);
            }
        });
    }

    @Override
    public void rebindSignalServer(final String sessionid) {
//        wsState = ConnectionState.NEW;
        wsState=ConnectionState.CONNECTING;
        wsClient.rebind(sessionid);
    }

    @Override
    public void reconnectSignalServer(AssistantConnectionParameters connectionParameters, final String sessionId) {

        this.connectionParameters = connectionParameters;
        Log.d(TAG, "on reconnectSignalServer entry!");

        if(wsState==ConnectionState.CONNECTING&&!TextUtils.isEmpty(sessionId)){
            LogUtils.i(TAG, "Ignore to reconnect when it is connecting");
            return;
        }

        executor.execute(new Runnable() {
            @Override
            public void run() {
                if(wsClient != null){
                    wsClient.disconnect(false);
                    wsClient = null;
                }
                connectToAssistantServerInternal(sessionId);
            }
        });
    }

    /**
     * Disconnect from Assistant Server.
     */
    @Override
    public void disconnectFromSignalServer() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                disconnectFromAssistantServerInternal();
            }
        });
        executor.requestStop();
    }



    /**
     * Connect to Assistant Server - function runs on a local looper thread.
     */
    private void connectToAssistantServerInternal(final String sessionId) {
        String connectionUrl = connectionParameters.wssUrl;
        String name = connectionParameters.username;
        String password = connectionParameters.password;
        LogUtils.i(TAG, "Connect to WebSocket Server: " + connectionUrl + " sessionid:"+sessionId);
        if(wsState==ConnectionState.CONNECTING&&!TextUtils.isEmpty(sessionId)){
            LogUtils.i(TAG, "Ignore to reconnect when it is connecting to WebSocket Server: " + connectionUrl);
            return;
        }
        if (wsClient != null){

            if (wsState == ConnectionState.CONNECTED){
                LogUtils.i(TAG,"WebSocket is connected,so ignore to login");
                events.onSignalServerConnected(sessionId);
                return;
            } else {
                wsClient.disconnect(true);
                wsClient = null;
            }
        }
        wsState = ConnectionState.CONNECTING;

        wsClient = new WebSocketChannelClient(executor, this);

        wsClient.connect(connectionUrl, name, password,sessionId);
    }


    public boolean isConnecting(){
        if(wsState == ConnectionState.NEW){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean isWsConnected(){
        return wsState == ConnectionState.CONNECTED;
    }

    /**
     * Disconnect from Assistant Server - function runs on a local looper thread.
     */
    private void disconnectFromAssistantServerInternal() {
        LogUtils.i(TAG, "Disconnect from WebSocket ServerRoom with state: " + wsState);
        wsState = ConnectionState.CLOSED;
        if (wsClient != null) {
            wsClient.disconnect(true);
            wsClient = null;
        }
    }

    /**
     * Helper functions to get websocket server url and username.
     */
//    private String getConnectionUrl(AssistantConnectionParameters connectionParameters) {
//        return connectionParameters.wssUrl;
//    }
//
//    private String getRegisterName(AssistantConnectionParameters connectionParameters) {
//        return connectionParameters.username;
//    }

    /**
     * Send local offer SDP to the other participant.
     */
    @Override
    public void sendCallStart(final String sessionId, final SessionDescription sdp) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (null == wsClient) {
                    LogUtils.i(TAG,"Send callstart,bt wsClient is null");
                    return;
                }
                JSONObject json = new JSONObject();
                jsonPut(json, "id", "callStart");
                if(!TextUtils.isEmpty(sessionId)){
                    jsonPut(json, "sid", sessionId);
                }
                jsonPut(json, "from", connectionParameters.username);
                jsonPut(json, "to", connectionParameters.customer);
                jsonPut(json, "sdpOffer", sdp.description);
                jsonPut(json, "token", wsClient.getToken());
                jsonPut(json, "rcuId", "");
                jsonPut(json, "urgent", PreferenceUtils.getPrefInt(BaseConstants.PRE_KEY_CALLEE,0));
                jsonPut(json, "appType", connectionParameters.robotType);
                jsonPut(json, "role", "client");
                jsonPut(json,"destroyMedia",connectionParameters.destroyMedia);
                LogUtils.i(TAG, "run: sendcallStart \n "+json.toString());
                wsClient.send(json.toString());
            }
        });
    }

    public void sendMessage(final String json){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                LogUtils.i(TAG,"run: sendMessage : "+json.toString());
                if (null == wsClient) {
                    LogUtils.i(TAG,"wsClient is null, return");
                    return;
                }
                wsClient.send(json);
            }
        });
    }
    @Override
    public void getStats(){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                JSONObject json = new JSONObject();
                jsonPut(json, "id", "getStats");
                wsClient.send(json.toString());
            }
        });
    }

    @Override
    public void sendInfo(final String sessionId, final JSONObject infoData) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (null == wsClient) {
                    LogUtils.i(TAG,"wsClient is null, return");
                    return;
                }
                if (!isWsConnected()){
                    try {
                        if (infoData.getString("type").equalsIgnoreCase("reportStatus")){
                            LogUtils.i(TAG,"Ready to send reportStatus,but ws is not connected");
                            return;
                        }
                    }catch (JSONException e){
                        return;
                    }
                }


                if (!infoData.has("id")){
                    jsonPut(infoData,"id","info");
                }

                if(!TextUtils.isEmpty(sessionId)){
                    jsonPut(infoData, "sid", sessionId);
                }
                jsonPut(infoData, "from", connectionParameters.username);
                String info = infoData.toString();
                LogUtils.i(TAG,"run: sendInfo "+info);
                wsClient.send(info);
            }
        });
    }

    @Override
    public void sendLocation(final String sessionId) {

    }

    @Override
    public void responseConnect(final String sessionId) {
//        executor.execute(new Runnable() {
//            @Override
//            public void run() {
//                JSONObject json = new JSONObject();
//                jsonPut(json, "id", "info");
//                if(!TextUtils.isEmpty(sessionId)){
//                    jsonPut(json, "sid", sessionId);
//                }
//                jsonPut(json, "from", connectionParameters.username);
//                jsonPut(json, "to", customer);
//                jsonPut(json, "type", "connect");
//                jsonPut(json, "data", "response");
//                wsClient.send(json.toString());
//            }
//        });
    }

    /*
     * 与客服的心跳
     * infoData
     * {
     *   pepperStatus:0/1    客户端与pepper连接状态 0:未连接 1:已连接
     *   asrStatus:0/1/2   语音识别状态 0:为开启 1:正常 2:异常
     * }
     */
    @Override
    public void sendHariInfo(final String sessionId, final String infoData) {
//        executor.execute(new Runnable() {
//            @Override
//            public void run() {
//                JSONObject json = new JSONObject();
//                jsonPut(json, "id", "info");
//                if(!TextUtils.isEmpty(sessionId)){
//                    jsonPut(json, "sid", sessionId);
//                }
//                jsonPut(json, "from", connectionParameters.username);
//                jsonPut(json, "to", customer);
//                jsonPut(json, "type", "hariInfo");
//                jsonPut(json, "data", infoData);
//                wsClient.send(json.toString());
//            }
//        });
    }

    /**
     * Send Ice candidate to the other participant.
     */
    @Override
    public void sendCallUpdate(final String sessionId, final IceCandidate candidate) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (null == wsClient) {
                    LogUtils.i(TAG,"wsClient is null, return");
                    return;
                }

                JSONObject json = new JSONObject();

                jsonPut(json, "id", "callUpdate");
                jsonPut(json, "sid", sessionId);
                jsonPut(json, "from", connectionParameters.username);
                jsonPut(json, "role","client");
                if(candidate!=null) {
                    JSONObject candidateJson = new JSONObject();

                    jsonPut(candidateJson, "candidate", candidate.sdp);
                    jsonPut(candidateJson, "sdpMid", candidate.sdpMid);
                    jsonPut(candidateJson, "sdpMLineIndex", candidate.sdpMLineIndex);

                    jsonPut(json, "candidate", candidateJson);
                }
                LogUtils.i(TAG,"run: sendCallUpdate");
                wsClient.send(json.toString());
            }
        });
    }

    @Override
    public void sendHeartbeat(final String sessionId) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (null == wsClient) {
                    LogUtils.i(TAG,"wsClient is null, return");
                    return;
                }

                JSONObject json = new JSONObject();

                jsonPut(json, "id", "heartbeat");
                jsonPut(json, "sid", sessionId);
                jsonPut(json, "from", connectionParameters.username);
                jsonPut(json, "role","client");
                LogUtils.i(TAG,"run: sendHeartbeat");
                wsClient.send(json.toString());
            }
        });
    }

    @Override
    public void sendCallStop(final String sessionId) {
        //Toast.makeText(BaseApplication.getInstance(),"send CallStop 1",Toast.LENGTH_LONG).show();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (null == wsClient) {
                    LogUtils.i(TAG,"wsClient is null, return");
                    return;
                }
                JSONObject json = new JSONObject();
                JSONObject candidateJson = new JSONObject();

                jsonPut(json, "id", "callStop");
                jsonPut(json, "sid", sessionId);
                jsonPut(json, "token", wsClient.getToken());
                jsonPut(json, "from", connectionParameters.username);

                LogUtils.i(TAG,"run: sendCallStop");
                wsClient.send(json.toString());
            }
        });
    }

    @Override
    public void logout(final String sessionId) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                JSONObject json = new JSONObject();
                try {
                    json.put("id", "logout");
                    json.put("name", connectionParameters.username);
                    json.put("sid", sessionId);
                    json.put("role","client");
                    json.put("password", connectionParameters.password);

                } catch (JSONException e) {
                    Log.e(TAG, "WebSocket register JSON error: " + e.getMessage());
                    return;
                }

                LogUtils.i(TAG, "run: sendLogout " + json.toString());
                wsClient.send(json.toString());
            }
        });
    }

    /**
     * WebSocketChannelEvents interface implementation.
     *
     * All events are called by WebSocketChannelClient on a local looper thread
     * (passed to WebSocket client constructor).
     */
    @Override
    public void onWebSocketMessage(final String msg) {
//        if (wsClient.getState() != WebSocketConnectionState.REGISTERED) {
//            Log.e(TAG, "Got WebSocket message in non registered state.");
//            return;
//        }
        try {
            JSONObject json = new JSONObject(msg);
            String msgId = json.getString("id");

//            if (msgId.equals("registerResponse")) {
//                String response = json.getString("response");
//                if (response.equals("rejected")) {
//                    //this case will be handled in websocketChannelClient
//                    String detail = json.getString("message");
//                    reportError("Failed to register: " + detail);
//                } else {
//                    Log.d(TAG, "Register to hari server completed.");
//                    wsState = ConnectionState.CONNECTED;
//                    events.onSignalServerConnected();
//                }
//            }
//            else

            //events.onChannelSignalError("WebSocket message JSON parsing error: " + e.toString());

            if (msgId.equals("callResponse")) {

                if(json.has("response")) {
                    String response = json.getString("response");
                    if (response.equalsIgnoreCase("ongoing")) {
                        //indicates the call session is created in server side but not accepted by HI
                        String sid = json.getString("sid");
                        if (!TextUtils.isEmpty(sid))
                            events.onCallSessionCreated(sid);
                        else
                            events.onChannelSignalError("Invalid sessionId within callStartResponse");
                    } else if (response.equals("rejected")) {
                        String detail = json.getString("message");
                        if(detail.equals("Busy, no free helper")){
                            events.onChannelCallRejected(detail);
                        }else if(detail.equals("Invalid session id")){
                            events.onChannelCallRejected("服务已结束");
                        }else {
                            events.onChannelCallRejected("Failed to call for assistance: " + detail);
                        }
                    } else { //accepted

                        String sid = json.getString("sid");
                        events.onCallSessionCreated(sid);

                        if (json.has("sdpAnswer")){
                            String answer = json.getString("sdpAnswer");
                            Log.d(TAG, "Received Sdp Answer: " + answer);
                            SessionDescription sdpAnswer = new SessionDescription(
                                    SessionDescription.Type.fromCanonicalForm("answer"), answer);
                            events.onCallAccepted(sdpAnswer);
                        }
                    }
                }else{
                    String reason = json.getString("reason");
//                    if(reason.equals("reject")){
                        if(reason.equals("reject")&&json.getString("message").equals("User declined"))
                        events.onChannelSignalError(reason);
                        else if(reason.equals("reject")&&json.getString("message").equals("User busy")){
                            events.onChannelSignalError(reason);
                        } else{
                        events.onChannelSignalError(json.getString("message"));
                    }
                }
            }
            else if (msgId.equals("callUpdate")) {
                //String peerName = json.getString("peer");
                IceCandidate candidate = null;
                if(json.has("candidate")){
                    JSONObject serverCandidate = json.getJSONObject("candidate");
                    Log.d(TAG, "IceCandidate Message Received: " +
                            serverCandidate.getString("candidate") + ", " +
                            serverCandidate.getString("sdpMid") + ", " +
                            serverCandidate.getString("sdpMLineIndex"));
                    candidate = new IceCandidate(serverCandidate.getString("sdpMid"),
                            serverCandidate.getInt("sdpMLineIndex"),
                            serverCandidate.getString("candidate"));
                }
                String sid = json.getString("sid");
                events.onCallUpdate(sid, candidate);
            }else if (msgId.equals("callUpdateResponse")) {
                //String peerName = json.getString("peer");
                IceCandidate candidate = null;
                String sid = json.getString("sid");
                events.onCallUpdate(sid, candidate);
            }else if (msgId.equals("heartbeatResponse")){
                String sid = json.getString("sid");
                events.onHeartbeatResponse(sid);
            }else if (msgId.equals("callStop")) {
                String detail ="";
                if(json.has("message"))
                    detail=json.getString("message");
                    String sid = json.getString("sid");
                    events.onCallStop(sid,detail);
            }else if (msgId.equals("objectIdentified")) {
                String objectName = json.getString("name");
                if (objectName != null && objectName.length() > 0) {
                    events.onObjectIdentified(objectName);
                }
            } else if (msgId.equals("faceIdentified")) {
                String faceName = json.getString("name");
                if (faceName != null && faceName.length() > 0) {
                    events.onFaceIdentified(faceName);
                }
            } else if (msgId.equals("navigationCommand")) {
                /*TODO
                type：0 代表模式切换
                command：1.人工模式；0.ai模式 （int）

                type：1 代表平移
                command：L 左 R 右 F 前进... （String）
                value：代表具体的数值。

                type：2 代表旋转
                command：代表旋转的角度 （double）
                 */
                String command = json.getString("command");
                if (command != null && command.length() > 0) {
                    events.onCommandReceived(json.toString());
//                    events.onCommandReceived(command);
                }
                Log.e(TAG,"naviCommand:"+json.toString());
            }else if(msgId.equals("slamNavigationStart")){
                String startPoint = json.getString("startingPoint");
                String finishingPoint = json.getString("finishingPoint");
                if(startPoint!=null && startPoint.length()>0 && finishingPoint!=null && finishingPoint.length()>0){
                    events.onSlamNavigationStart(startPoint, finishingPoint);
                }
            }else if(msgId.equals("info")){

                LogUtils.i(TAG, "ws: onInfo"+json );

                events.onInfo(json);

            }
        } catch (JSONException e) {
            events.onChannelSignalError("WebSocket message JSON parsing error: " + e.toString());
        }
    }

    @Override
    public void onWebSocketClose() {
        LogUtils.i(TAG, "onWebSocketClose");
        wsState = ConnectionState.CLOSED;
        events.onChannelClose();
    }

    @Override
    public void onWebSocketConnectError(final String errorMessage) {
        LogUtils.i(TAG, errorMessage);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (wsState != ConnectionState.ERROR) {
                    wsState = ConnectionState.ERROR;
                }
                events.onChannelConnectionError(errorMessage);
            }
        });
    }

    @Override
    public void onWebSocketRegisterError(final String errorMessage) {
        LogUtils.i(TAG, errorMessage);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (wsState != ConnectionState.ERROR) {
                    wsState = ConnectionState.ERROR;
                }

                events.onChannelAuthenticationError(errorMessage);
            }
        });
    }

    @Override
    public void onWebSocketRegisterred(final String sessionId) {
        wsState = ConnectionState.CONNECTED;
        events.onSignalServerConnected(sessionId);
    }


    // Put a |key|->|value| mapping in |json|.
    private static void jsonPut(JSONObject json, String key, Object value) {
        try {
            json.put(key, value);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
