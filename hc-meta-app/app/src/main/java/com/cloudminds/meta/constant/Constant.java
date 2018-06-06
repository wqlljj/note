package com.cloudminds.meta.constant;

import com.cloudminds.meta.bean.ChatMessage;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by tiger on 17-3-31.
 */

public class Constant {
    public final static String FIRST_LOGIN = "first_login";
    public final static int DEFAULT_SPIV = -1;

    public final static int FAMILY_TYPE_MANAGE= 0;
    public final static int FAMILY_TYPE_ADD = 1;
    public final static int FAMILY_TYPE_UPDATE = 2;

    public final static int FAMILY_DO_ADD = 1;
    public final static int FAMILY_DO_UPDATE = 2;
    public final static int FAMILY_DO_BACK = 3;
    public final static int FAMILY_DO_ADD_BACK = 4;

    public final static String BAIDU_TTS_APPID = "9417717";
    public final static String BAIDU_TTS_APPKEY = "UriAkx385VgpQ2geNedKxV6I";
    public final static String BAIDU_TTS_SCREENKEY = "ddb5f67e5624fdce73c347fddf9745f1";
    public final static String SAMPLE_DIR_NAME = "baiduTTS";

    public final static String SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female.dat";
    public final static String SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male.dat";
    public final static String TEXT_MODEL_NAME = "bd_etts_text.dat";
    public final static String ENGLISH_SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female_en.dat";
    public final static String ENGLISH_SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male_en.dat";
    public final static String ENGLISH_TEXT_MODEL_NAME = "bd_etts_text_en.dat";

    public final static String SPEECH_GRAMMAR = "baidu_speech_grammar.bsg";
    public final static String SPEECH_S_1 = "s_1";
    public final static String WAKE_UP = "WakeUp.bin";

    public final static String SPEECH_MESSAGE_KEY = "speech_message";

    public final static int HUB_CONN_NOMAL = 0;
    public final static int HUB_CONN_ON_CONNECTION = 1;
    public final static int HUB_CONN_IN_CONNECTION = 2;
    public final static int HUB_CONN_DISCONNECT = 3;
    public final static int HUB_CONN_END = 4;
    public final static int CALL_FAILED=5;
    public final static int CALL_CLOSED=6;

    public final static int HUB_SINGLE_CLICK = 1;
    public final static int HUB_DOUBLE_CLICK = 2;

    public final static int SINGLE_CLICK_BACK = 3;
    public final static int DOUBLE_CLICK_BACK = 4;

    public final static String FIRST_ACTIVATE = "first_activate";

    public final static String REGISTER_SUCCESS = "200";
    public final static String REGISTER_USER_NOT_EXIST = "1002";
    public final static String REGISTER_PASSWORD_ERROR = "1003";
    public final static String REGISTER_USER_NAME_OR_PASSWORD_EMPTY = "1";
    public final static String REGISTER_NETWORK_UNAVAILABLE = "3";
    public final static String REGISTER_ON_FAILURE = "4";

    public final static String CALLSTART = "callstart";
    public final static String CALLEND = "callend";

    public final static String PRE_KEY_NO_META="com.cloudminds.hc.hariservice.key.NO_META";
    public final static String PRE_KEY_STARTCALL="com.cloudminds.hc.hariservice.key.STARTCALL";
    public final static String PRE_KEY_RESTARTCALL="com.cloudminds.hc.hariservice.key.RESTARTCALL";
    public final static String PRE_KEY_CALLCALLEE="com.cloudminds.hc.hariservice.key.CALLCALLEE";

    //聊天记录
    public final static ArrayList<ChatMessage> msg=new ArrayList<>();

}
