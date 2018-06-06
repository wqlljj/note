package ai.kitt.snowboy;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;

import ai.kitt.snowboy.audio.AudioDataSaver;
import ai.kitt.snowboy.audio.PlaybackThread;
import ai.kitt.snowboy.audio.RecordingThread;

/**
 * Created by wangqi on 2018/5/21.
 */

public class SnowboyClient {
    private static int preVolume = -1;
    private static SnowboyClient snowboyClient;
    private  Context context;
    private  int activeTimes;
    private final RecordingThread recordingThread;
    private final PlaybackThread playbackThread;
    private String TAG="SnowboyClient";
    private SnowboyCallBack callBack;
    public static void init(Context context){
        if(snowboyClient==null) {
            snowboyClient = new SnowboyClient(context);
        }
    }
    public static SnowboyClient getInstance(){
            return snowboyClient;
    }
    public boolean isRecording(){
        return recordingThread.isRecording();
    }
    public void startRecording() {
        recordingThread.startRecording();
        updateLog(" ----> recording started ...", "green");
    }
    public void stopRecording() {
        recordingThread.stopRecording();
        updateLog(" ----> recording stopped ", "green");
    }
    public void startPlayback() {
        updateLog(" ----> playback started ...", "green");
        // (new PcmPlayer()).playPCM();
        playbackThread.startPlayback();
    }

    public void stopPlayback() {
        updateLog(" ----> playback stopped ", "green");
        playbackThread.stopPlayback();
    }

    public void sleep() {
        try {
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SnowboyCallBack getCallBack() {
        return callBack;
    }

    public void setCallBack(SnowboyCallBack callBack) {
        this.callBack = callBack;
    }

    public SnowboyClient(Context context) {
        this.context = context;
//        setProperVolume(context);
        AppResCopy.copyResFromAssetsToSD(context);
        activeTimes = 0;

        recordingThread = new RecordingThread(handle, new AudioDataSaver());
        playbackThread = new PlaybackThread();
    }
    public Handler handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            MsgEnum message = MsgEnum.getMsgEnum(msg.what);
            switch(message) {
                case MSG_ACTIVE:
                    activeTimes++;
                    updateLog(" ----> Detected " + activeTimes + " times", "green");
                    // Toast.makeText(Demo.this, "Active "+activeTimes, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "handleMessage: Active "+activeTimes);
                    if(callBack!=null)
                    callBack.onEvent(SnowboyCallBack.Event.ACTIVE);
                    break;
                case MSG_INFO:
                    updateLog(" ----> "+message,"green");
                    break;
                case MSG_VAD_SPEECH:
                    updateLog(" ----> normal voice", "blue");
                    break;
                case MSG_VAD_NOSPEECH:
                    updateLog(" ----> no speech", "blue");
                    break;
                case MSG_ERROR:
                    updateLog(" ----> " + msg.toString(), "red");
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    private    void setProperVolume(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        preVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        updateLog(" ----> preVolume = "+preVolume, "green");
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        updateLog(" ----> maxVolume = "+maxVolume, "green");
        int properVolume = (int) ((float) maxVolume * 0.2);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, properVolume, 0);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        updateLog(" ----> currentVolume = "+currentVolume, "green");
    }
    public void updateLog(final String text, final String color) {
        String str = "<font color='"+color+"'>"+text+"</font>"+"<br>";
        Log.e(TAG, "updateLog: "+ Html.fromHtml(str) );
    }
    private void restoreVolume() {
        if(preVolume>=0) {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, preVolume, 0);
            updateLog(" ----> set preVolume = "+preVolume, "green");
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            updateLog(" ----> currentVolume = "+currentVolume, "green");
        }
    }
    public void onDestroy() {
//        restoreVolume();
        recordingThread.stopRecording();
        context=null;
    }
}
