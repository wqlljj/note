package com.cloudminds.meta.util;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

/**
 * Created by SX on 2017/12/6.
 */

public class CustomAudioManager {
    private final AudioManager audioManager;

    public CustomAudioManager(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public static CustomAudioManager getInstance(Context context) {
        return new CustomAudioManager(context);
    }

    /**
     * 切换到外放
     */
    public void changeToSpeaker() {
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setSpeakerphoneOn(true);
    }

    /**
     * 切换到耳机模式
     */
    public void changeToHeadset() {
        audioManager.setSpeakerphoneOn(false);
    }

    /**
     * 切换到听筒
     */
    public void changeToReceiver() {
        audioManager.setSpeakerphoneOn(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        } else {
            audioManager.setMode(AudioManager.MODE_IN_CALL);
        }

    }
}
