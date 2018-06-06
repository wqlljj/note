package com.cloudminds.hc.hariservice.utils;


public class BaseConstants {

//	public static  String HTTP_REQUEST = "http://111.13.139.178:20080";
	public static String HTTP_REQUEST ="http://smartvoice.cloudminds.com";
	public static String AGENID = "1";
	public static String TOKENKEY_ON = "57fd8af5fa80f717347e87a762b9d1eb";
	public static String TOKENKEY_OFF = "ebad65761f6fd6474b4b476474f31b1c";

//	public static final String SERVER_ADDRESS = "192.168.1.2";//
	public static  String SERVER_ADDRESS = "111.13.138.130";//DeskPC: 10.11.32.23； DemoServer: 10.11.33.72,223.72.137.6 ; Demo Hari: 111.13.138.130  //10.11.32.172
	public static  String SERVER_PORT="9001";
	public static final String ROC_SERVER = "10.11.35.198";
	public static final String ROC_PORT = "8010";

	public static String PRE_KEY_ACCOUNT ="com.cloudminds.hc.hariservice.key.account";
	public static String PRE_KEY_PASSWORD ="com.cloudminds.hc.hariservice.key.password";
	public static String PRE_KEY_ROBOT_TYPE ="com.cloudminds.hc.hariservice.key.robotType";
	public static String PRE_KEY_SERVER_ADDRESS = "com.cloudminds.hc.hariservice.key.server";
	public static String PRE_KEY_SERVER_PORT = "com.cloudminds.hc.hariservice.key.serverPort";
    public static String PRE_KEY_WEBRTC_REALY_ENABLE = "com.cloudminds.hc.hariservice.key.webrtc.relay.enable";
	public static String PRE_KEY_CUSTOMER = "com.cloudminds.hc.hariservice.key.customer";
	public static String PRE_KEY_RCUID = "com.cloudminds.hc.hariservice.key.rcuid";
	public static String PRE_KEY_TENANTID = "com.cloudminds.hc.hariservice.key.tenantid";
	public static String PRE_KEY_VIDEO_BPS = "com.cloudminds.hc.hariservice.key.videoBps";
	public static String PRE_KEY_VIDEO_STARTBPS = "com.cloudminds.hc.hariservice.key.videoStartBps";
	public static String PRE_KEY_AUDIO_STARTBPS = "com.cloudminds.hc.hariservice.key.audioStartBps";
	public static String PRE_KEY_VIDEO_CODEC = "com.cloudminds.hc.hariservice.key.videoCodec";
	public static String PRE_KEY_AUDIO_CODEC = "com.cloudminds.hc.hariservice.key.audioCodec";
	public static String PRE_KEY_VIDEO_FPS = "com.cloudminds.hc.hariservice.key.videoFps";
	public static String PRE_KEY_VIDEO_WIDTH = "com.cloudminds.hc.hariservice.key.videoWidth";
	public static String PRE_KEY_VIDEO_HEIGHT = "com.cloudminds.hc.hariservice.key.videoHeight";
	public static String PRE_KEY_MS_MAX_BPS = "com.cloudminds.hc.hariservice.key.msMaxBps";
	public static String PRE_KEY_MS_MIN_BPS = "com.cloudminds.hc.hariservice.key.msMinBps";
	public static String PRE_KEY_CALLEE = "com.cloudminds.hc.hariservice.key.callee";
	public static String PRE_KEY_SESSION_ID = "com.cloudminds.hc.hariservice.key.sessionid";

	public static String PRE_KEY_MONITOR_ROBOT_INFO_ENABLE = "com.cloudminds.hc.hariservice.key.monitor.robot.info";
	public static String PRE_KEY_MONITOR_ROBOT_INFO_RATE = "com.cloudminds.hc.hariservice.key.monitor.robot.info.rate";

	public static String PRE_KEY_YM_SERVER = "com.cloudminds.hc.hariservice.key.ymServer";
	public static String PRE_KEY_YM_PORT = "com.cloudminds.hc.hariservice.key.ymPort";

	public static String PRE_KEY_LOG_ENABLE = "com.cloudminds.hc.hariservice.key.log.enable";
	public static String PRE_KEY_RCU_FIRST_CONNECT = "com.cloudminds.hc.hariservice.key.rcu.firstconnect";

	public static String PRE_KEY_ROBOTID = "com.cloudminds.hc.hariservice.key.robotid";
	public static String PRE_KEY_CALL_MODE = "com.cloudminds.hc.hariservice.key.callMode";

	public static final String MODE= "mode";//
	public static final String SPEAKPHONE= "speakphone";//麦克风

	/**
	 * webrtc 默认参数值
	 **/
	public static final int DEFAULT_VIDEO_STARTBPS = 512;
	public static final int DEFAULT_VIDEO_FPS = 15;
	public static final int DEFAULT_AUDIO_STARTBPS = 50;
	public static final String  DEFAULT_AUDIO_CODEC = "OPUS";
	public static final String  DEFAULT_VIDEO_CODEC = "VP8";
	public static final int DEFAULT_VIDEO_WIDTH = 640;
	public static final int DEFAULT_VIDEO_HEIGHT = 480;



	/**
	 * event 优先级
	 * */
	public static final int SERVICE_EVENTBUS_PRIORITY = 10;
	public static final int MESSAGE_EVENTBUS_PRIORITY = 100;

	public static final String INTENT_ACTION_HARI_SERVICE = "hari.service";

}
