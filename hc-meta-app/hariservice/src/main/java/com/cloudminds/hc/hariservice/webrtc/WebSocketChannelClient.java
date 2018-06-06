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

import com.cloudminds.hc.hariservice.command.CDGenerator;
import com.cloudminds.hc.hariservice.utils.BaseConstants;
import com.cloudminds.hc.hariservice.utils.Log.LogUtils;
import com.cloudminds.hc.hariservice.utils.PreferenceUtils;

import de.tavendo.autobahn.WebSocket;
import de.tavendo.autobahn.WebSocketOptions;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketConnectionHandler;

/**
 * WebSocket client implementation.
 *
 * <p>All public methods should be called from a looper executor thread
 * passed in a constructor, otherwise exception will be thrown.
 * All events are dispatched on the same thread.
 */

public class WebSocketChannelClient {
    private static final String TAG = "HS/WSChannelClient";
    private static final int CLOSE_TIMEOUT = 1000;
    private final WebSocketChannelEvents events;
    private final LooperExecutor executor;
    private WebSocketConnection ws;
    private WebSocketConnectionHandler wsHandler;
    private String wsServerUrl;
    private String regName;
    private String regPwd;
    private String token;
    private WebSocketConnectionState state;
    private final Object closeEventLock = new Object();
    private boolean closeEvent;
    // WebSocket send queue. Messages are added to the queue when WebSocket
    // client is not registered and are consumed in register() call.
    private final LinkedList<String> wsSendQueue;

    public String getToken(){
        return token;
    }

    /**
     * Possible WebSocket connection states.
     */
    public enum WebSocketConnectionState {
        NEW, CONNECTED, REGISTERED, CLOSED, ERROR
    };


    /**
     * Callback interface for messages delivered on WebSocket.
     * All events are dispatched from a looper executor thread.
     */
    public interface WebSocketChannelEvents {
        public void onWebSocketMessage(final String message);
        public void onWebSocketClose();
        public void onWebSocketConnectError(final String description);
        public void onWebSocketRegisterError(final String description);
        public void onWebSocketRegisterred(final String sessionId);
    }

    public WebSocketChannelClient(LooperExecutor executor, WebSocketChannelEvents events) {
        this.executor = executor;
        this.events = events;
        wsSendQueue = new LinkedList<String>();
        state = WebSocketConnectionState.NEW;
    }

    public WebSocketConnectionState getState() {
        return state;
    }

    public void connect(final String wsUrl, final String name, final  String password, final String sessionId) {
        checkIfCalledOnValidThread();
        LogUtils.i(TAG,"On connect entry!");
        if (state != WebSocketConnectionState.NEW&&state!=WebSocketConnectionState.CLOSED) {
            LogUtils.i(TAG, "WebSocket is already connected.");
            return;
        }
        wsServerUrl = wsUrl;
        regName = name;
        regPwd = password;
        closeEvent = false;

        LogUtils.i(TAG, "Connecting WebSocket to: " + wsUrl + ".   regName = "+regName);
        WebSocketOptions options = new WebSocketOptions();
        options.setSocketReceiveTimeout(200);//default is 200 ms
        ws = new WebSocketConnection();
        wsHandler = new WebSocketConnectionHandler() {
            @Override
            public void onOpen() {
                LogUtils.i(TAG, "WebSocket connection opened to: " + wsServerUrl);
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        state = WebSocketConnectionState.CONNECTED;
                        register(regName,regPwd,sessionId);
                    }
                });
            }

            @Override
            public void onClose(WebSocketCloseNotification code, String reason) {
                LogUtils.i(TAG, "WebSocket connection closed. Code: " + code
                        + ". Reason: " + reason + ". State: " + state);
                //断网CODE=5
                synchronized (closeEventLock) {
                    closeEvent = true;
                    closeEventLock.notify();
                }
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (state != WebSocketConnectionState.CLOSED) {
                                state = WebSocketConnectionState.CLOSED;
                                events.onWebSocketClose();
                            }
                        }
                    });
            }
            boolean webSocketClosing=false;
            @Override
            public void onTextMessage(String payload) {
                Log.d(TAG, "WSS->C: " + payload);
                final String message = payload;
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (state != WebSocketConnectionState.REGISTERED) {

                            try {
                                JSONObject json = new JSONObject(message);
                                String msgId = json.getString("id");

                                if (msgId.equals("loginResponse")) {
                                    String response = json.getString("response");
                                    LogUtils.i(TAG, "run: loginResponse "+response);
                                    if (response.equals("rejected")) {
                                        String detail = json.getString("message");
                                        final String errorMessage = "Failed to register: " + detail;
                                        Log.e(TAG, errorMessage);
                                        if (state != WebSocketConnectionState.ERROR) {
                                            state = WebSocketConnectionState.ERROR;
                                        }
                                        events.onWebSocketRegisterError(errorMessage);
                                    } else {
                                        LogUtils.i(TAG, "Register to assistant websocket server completed.");
                                        state = WebSocketConnectionState.REGISTERED;
                                        token = json.getString("token");
                                        //webSocketClosing=false;
                                        // Send any previously accumulated messages.
                                        Log.i("TEST", "run: 连接成功");
                                        for (String sendMessage : wsSendQueue) {
                                            send(sendMessage);
                                        }
                                        wsSendQueue.clear();

                                        String sessionId = "";
                                        boolean isNeedMediaReconnect = false;
                                        if (json.has("data")){
                                            JSONObject data = json.getJSONObject("data");
                                            sessionId = data.getString("sid");
                                            if (data.has("mediaReconnect")){
                                                isNeedMediaReconnect = true;
                                            }
                                        }
                                        events.onWebSocketRegisterred(sessionId);

                                        if (isNeedMediaReconnect){
                                            events.onWebSocketMessage("{\"id\":\"info\",\"type\":\"mediaReconnect\"}");
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "WebSocket message JSON received from HariServer parsing error: " + e.toString());
                            }
                        }

                        if (state == WebSocketConnectionState.CONNECTED
                                || state == WebSocketConnectionState.REGISTERED) {
                            events.onWebSocketMessage(message);
                        }
                    }
                });
            }

            @Override
            public void onRawTextMessage(byte[] payload) {

            }

            @Override
            public void onBinaryMessage(byte[] payload) {

            }
        };

        try {
            LogUtils.i(TAG, "  WebSocket connect...");
            ws.connect(new URI(wsUrl), wsHandler,options);
        } catch (WebSocketException e) {
//            reportError("WebSocket connection error: " + e.getMessage());
            final String errorMessage = "WebSocket connection error: " + e.getMessage();
            LogUtils.i(TAG, errorMessage);
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (state != WebSocketConnectionState.ERROR) {
                        state = WebSocketConnectionState.ERROR;
                        events.onWebSocketConnectError(errorMessage);
                    }
                }
            });
        }catch (URISyntaxException e) {
            String message = e.getLocalizedMessage();
            LogUtils.i(TAG, message);
        }
    }

    public void rebind(final String sessionId)
    {
        LogUtils.i(TAG, "Rebind WebSocket ... with username: " + regName);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                state = WebSocketConnectionState.CONNECTED;
                register(regName, regPwd,sessionId);
            }
        });
    }

    public void register(final String regName, final String pwd, final String sessionId) {
        checkIfCalledOnValidThread();
        this.regName = regName;
        if (state != WebSocketConnectionState.CONNECTED) {
            Log.w(TAG, "WebSocket register() in state " + state);
            return;
        }
        Log.d(TAG, "Registering name " + regName + ".");
        JSONObject json = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            json.put("id", "login");
            json.put("name", regName);//regName+"#"+PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_TENANTID,"10086")
            if(!TextUtils.isEmpty(sessionId))
                json.put("sid", sessionId);
            json.put("role", "client");
            json.put("password", pwd);
            json.put("appType", PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_ROBOT_TYPE,"meta2"));

            data.put("tenantId",PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_TENANTID,"10086"));
            String rcuId = PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_RCUID,"2919276686923065298");
            data.put("rcuId", rcuId);
            data.put("lang", CDGenerator.getSysLanguage());
            data.put("robotId", PreferenceUtils.getPrefString(BaseConstants.PRE_KEY_ROBOTID,"11111111"));
            data.put("rcuFirstConnection", PreferenceUtils.getPrefBoolean(BaseConstants.PRE_KEY_RCU_FIRST_CONNECT,false));
            data.put("rcuFirstConnectionDate", String.valueOf(System.currentTimeMillis()));

            json.put("data",data);

        } catch (JSONException e) {
            Log.e(TAG, "WebSocket register JSON error: " + e.getMessage());
            return;
        }

        Log.d(TAG, "C->WSS: " + json.toString());
        ws.sendTextMessage(json.toString());

    }


    public void send(String message) {
        checkIfCalledOnValidThread();
        switch (state) {
            case NEW:
            case CONNECTED:
            // Store outgoing messages and send them after websocket client
            // is registered.
            //LogUtils.d(TAG, "WS ACC: " + message);
                wsSendQueue.add(message);
                return;
            case ERROR:
            case CLOSED:
                //LogUtils.e(TAG, "WebSocket send() in error or closed state : " + message);
                return;
            case REGISTERED:
                //LogUtils.d(TAG, "C->WSS: " + message);
                ws.sendTextMessage(message);
                break;
        }
        return;
    }


  public void disconnect(boolean waitForComplete) {
    checkIfCalledOnValidThread();
    LogUtils.i(TAG, "Disonnect WebSocket. State: " + state);

    // Close WebSocket in CONNECTED or ERROR states only.
    if (state == WebSocketConnectionState.CONNECTED
        || state == WebSocketConnectionState.REGISTERED
        || state == WebSocketConnectionState.ERROR) {

      if(ws!=null)
            ws.disconnect();
      state = WebSocketConnectionState.CLOSED;

      // Wait for websocket close event to prevent websocket library from
      // sending any pending messages to deleted looper thread.
      if (waitForComplete) {
        synchronized (closeEventLock) {
          while (!closeEvent) {
            try {
              closeEventLock.wait(CLOSE_TIMEOUT);
              break;
            } catch (InterruptedException e) {
              Log.e(TAG, "Wait error: " + e.toString());
            }
          }
        }
      }
    }
    LogUtils.i(TAG, "Disonnecting WebSocket done.");
  }

//  private void reportError(final String errorMessage) {
//    Log.e(TAG, errorMessage);
//    executor.execute(new Runnable() {
//      @Override
//      public void run() {
//        if (state != WebSocketConnectionState.ERROR) {
//          state = WebSocketConnectionState.ERROR;
//          events.onWebSocketError(errorMessage);
//        }
//      }
//    });
//  }

   // Helper method for debugging purposes. Ensures that WebSocket method is
   // called on a looper thread.
  private void checkIfCalledOnValidThread() {
    if (!executor.checkOnLooperThread()) {
      throw new IllegalStateException(
          "WebSocket method is not called on valid thread");
    }
  }
}
