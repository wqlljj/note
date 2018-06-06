package com.cloudminds.meta.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.SoundPool;
import android.util.Log;

import com.cloudminds.meta.R;
import com.cloudminds.meta.application.MetaApplication;
import com.cloudminds.meta.bean.ChatMessage;
import com.cloudminds.meta.manager.EventManager;
import com.cloudminds.meta.service.asr.BusEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.greenrobot.event.EventBus;

public class PlayerUtil implements OnCompletionListener, OnPreparedListener {

	private final Context mContext;
	private final ExecutorService singleThreadPool;
	public MediaPlayer mediaPlayer; // 媒体播放器
	public int selectPlay = -1;
	public static final int MUSIC = 0;
	public static final int RESOURCE = 1;
	public  MusicPlayHandler musicPlayHandler = null;
	private String TAG="mediaPlayer";
	private static PlayerUtil playerUtil;
	private SoundPool soundPool;

	// 初始化播放器
	private PlayerUtil(Context context) {
		super();
		mContext = context;
		 singleThreadPool = Executors.newSingleThreadExecutor();
		soundPool= new SoundPool(4,AudioManager.STREAM_SYSTEM,5);

		soundPool.load(context, R.raw.bdspeech_recognition_start,1);
		soundPool.load(context, R.raw.bdspeech_recognition_error,2);
		soundPool.load(context, R.raw.beep,3);
	}
	public static PlayerUtil getPlayerUtil(Context context){
		if(playerUtil==null)
		playerUtil = new PlayerUtil(context);
		return playerUtil;
	}

	public void playUrl(final String url) {
		Log.e(TAG, "playUrl: "+url );
		if(!EventManager.getInstance().isEnablePlayMisic()){
			MetaApplication.addMessage(ChatMessage.Type.CHAT_LEFT,mContext.getString(R.string.music_defer_play));
			EventManager.getInstance().setMusicState(EventManager.MUSIC_NEEDPLAY,url);
			return;
		}
		MetaApplication.addMessage(ChatMessage.Type.CHAT_LEFT,mContext.getString(R.string.music_playing));
		EventBus.getDefault().post(new BusEvent(BusEvent.Event.PLAYMUSIC_START));
		MessageHandleThreadFactory.getInstance().addTask(new Runnable() {

			@Override
			public void run() {
				try {
					if(mediaPlayer==null)
					mediaPlayer = new MediaPlayer();
					mediaPlayer.setOnPreparedListener(playerUtil);
					mediaPlayer.reset();
					selectPlay = MUSIC;
					mediaPlayer.setDataSource(url); // 设置数据源
					mediaPlayer.prepareAsync();
				} catch (Exception e) {
					e.printStackTrace();
					EventBus.getDefault().post(new BusEvent(BusEvent.Event.PLAYMUSIC_FINISH));
				}

			}
		});
	}
	public static boolean isCallBack=true;
	public  void playDi(final Context context, final int resource) {
		isCallBack=false;
		Future<?> submit = singleThreadPool.submit(new Runnable() {
			@Override
			public void run() {
				try {
//					Log.e(TAG, "playDi: "+selectPlay+"   "+resource );
					if (selectPlay == resource) return;
					Log.e(TAG, "playDi: start " + resource + "   " + selectPlay);
					selectPlay = RESOURCE;
					final MediaPlayer mediaPlayer = MediaPlayer.create(mContext, resource);// 播放固定资源
					mediaPlayer.start();
					mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
						@Override
						public void onCompletion(MediaPlayer mp) {
							if (musicPlayHandler != null) {
								isCallBack=true;
								musicPlayHandler.onMusic(2, resource);
								Log.e(TAG, "playDi: onCompletion:    " + resource);
								if(mediaPlayer!=null)
								mediaPlayer.release();
								selectPlay = -1;
							}
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "playDi: error  " + resource);
					isCallBack=true;
					musicPlayHandler.onMusic(2, resource);
				}
			}
		});
		try {
			if(submit.get()==null){//如果Future's get返回null，任务完成
				System.out.println("任务完成");
				if(!isCallBack){
					isCallBack=true;
					musicPlayHandler.onMusic(2, resource);
				}
			}
		}  catch (Exception e) {
			//否则我们可以看看任务失败的原因是什么
			System.out.println(e.getMessage());
			if(!isCallBack){
				isCallBack=true;
				musicPlayHandler.onMusic(2, resource);
			}
		}


	}

	// 暂停
	public void pause() {
		mediaPlayer.pause();
	}

	// 停止
	public void stop() {
		if (musicPlayHandler != null) {
			musicPlayHandler.onMusic(1, selectPlay);
		}
		if (mediaPlayer != null) {
			mediaPlayer.stop();
		}
		EventBus.getDefault().post(new BusEvent(BusEvent.Event.PLAYMUSIC_FINISH));
	}

	// 开始
	public void start() {
		if (musicPlayHandler != null) {
			musicPlayHandler.onMusic(3, selectPlay);
		}
		if (mediaPlayer != null) {
			mediaPlayer.start();
		}
	}

	public void release() {
		if (mediaPlayer != null) {
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		if (musicPlayHandler != null) {
			musicPlayHandler.onMusic(0, selectPlay);
		}
		 mp.start();
		Log.e("playUrl", "onPrepared");
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (musicPlayHandler != null) {
			musicPlayHandler.onMusic(2, selectPlay);
		}
		EventBus.getDefault().post(new BusEvent(BusEvent.Event.PLAYMUSIC_FINISH));
		Log.e("mediaPlayer", "onCompletion");
	}

	public boolean isPlayering() {
		if (mediaPlayer != null) {
			return mediaPlayer.isPlaying();
		} else {
			return false;
		}

	}

	public static abstract interface MusicPlayHandler {
		public abstract void onMusic(int status, int select);// 0:onPrepared
																// 1:stop
																// 2.onCompletion
	}

}