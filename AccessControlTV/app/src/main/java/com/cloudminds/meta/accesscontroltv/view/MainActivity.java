package com.cloudminds.meta.accesscontroltv.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


import com.brioal.adtextviewlib.view.ADTextView;
import com.brioal.adtextviewlib.view.OnAdChangeListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cloudminds.meta.accesscontroltv.bean.NewsBean;
import com.cloudminds.meta.accesscontroltv.broadcast.USBBroadcastReceiver;
import com.cloudminds.meta.accesscontroltv.constant.Constant;
import com.cloudminds.meta.accesscontroltv.hcsdk.HCClient;
import com.cloudminds.meta.accesscontroltv.mqtt.MQTTService;
import com.cloudminds.meta.accesscontroltv.bean.PersonInfoBean;
import com.cloudminds.meta.accesscontroltv.R;
import com.cloudminds.meta.accesscontroltv.persenter.MainPersenter;
import com.cloudminds.meta.accesscontroltv.util.DateUtil;
import com.cloudminds.meta.accesscontroltv.util.LogUtils;
import com.cloudminds.meta.accesscontroltv.util.SharePreferenceUtils;
import com.cloudminds.meta.accesscontroltv.util.ToastUtil;
import com.cloudminds.meta.accesscontroltv.util.Utils;
import com.cloudminds.meta.accesscontroltv.view.adapter.MainAdapter;
import com.github.onlynight.multithreaddownloader.library.DownloadManager;
import com.github.onlynight.multithreaddownloader.library.FileDownloader;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cloudminds.meta.accesscontroltv.constant.Constant.MAIN_BG_KEY;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends Activity implements InterFaceView, MediaPlayer.OnInfoListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, View.OnClickListener, MediaPlayer.OnPreparedListener, USBBroadcastReceiver.OnUSBListener {
    private MainPersenter persenter;
    private RecyclerView recyclerView;
    private MainAdapter mainAdapter;
    private String savePath;
    private String fileName="给你们1.mp4";
    private Uri uri;
    private static String TAG="APP/MainActivity";
    private ImageView mainBackground;
    private String main_bg_url;
    private VideoView mVideoView;
    private View videoLayout;
    private View welcomeLayout;
    private int type=-1;
    private TextView welcomeMsg;
    private View personInfoLayout;
    private View show2;
    private ADTextView adTextView;
    private SurfaceView mSurface;
    private HCClient hcClient;
    private USBBroadcastReceiver usbBroadcastReceiver;
    private File[] videoPaths;
    private int playVideoIndex=-1;
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //定义全屏参数
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //获得当前窗体对象
        Window window = MainActivity.this.getWindow();
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);
        setContentView(R.layout.activity_main_1);
        persenter=new MainPersenter(this);
        initDebug();
        initView();
        initData();

    }
    public static File getInternalCacheDirectory(Context context,String type) {
        File appCacheDir = null;
        if (TextUtils.isEmpty(type)){
            appCacheDir = context.getCacheDir();// /data/data/app_package_name/cache
        }else {
            appCacheDir = new File(context.getFilesDir(),type);// /data/data/app_package_name/files/type
        }

        if (!appCacheDir.exists()&&!appCacheDir.mkdirs()){
            Log.e(TAG,"getInternalDirectory fail ,the reason is make directory fail !");
        }
        return appCacheDir;
    }
    public static boolean isDebug=false;
    private void initDebug() {
        findViewById(R.id.debug).setVisibility(isDebug?View.VISIBLE:View.GONE);
        findViewById(R.id.publishBg).setOnClickListener(new View.OnClickListener() {
            String[] images=new String[]{"/aifile/screen/timg.jpg",
                    "/aifile/screen/hari_dev_plan_Q2.png"};
            int i=0;
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick: 背景" );
//                playVideo("");
//                int i=10/0;
//                MQTTService.publish("{\"status\": \"screen\", \"category\": \"screen\", \"list\": \""+images[(i++)%2]+"\"}");
                if(hcClient.loginNormalDevice()==-1){
                    Toast.makeText(MainActivity.this, "连接摄像头失败", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this, "连接摄像头成功", Toast.LENGTH_SHORT).show();
                    hcClient.preview();
                }
                File rootDirectory = Environment.getRootDirectory();
                Log.e(TAG, "onClick: rootDirectory" +rootDirectory.getAbsolutePath() );
                for (String s : rootDirectory.list()) {
                    Log.e(TAG, "onClick: "+s );
                }
                File externalStorageDirectory = Environment.getExternalStorageDirectory();
                Log.e(TAG, "onClick: externalStorageDirectory "+externalStorageDirectory.getAbsolutePath() );
                for (String s : externalStorageDirectory.list()) {
                    Log.e(TAG, "onClick: "+s );
                }
                File parentFile = externalStorageDirectory.getParentFile();
                Log.e(TAG, "onClick: parentFile  "+parentFile.getAbsolutePath() );
                for (String s : parentFile.list()) {
                    Log.e(TAG, "onClick: "+s );
                    MQTTService.publish("{\"status\": \"AAA\", \"category\": \"AAA\", \"list\": \""+s+"\"}");
                }
            }
        });
        findViewById(R.id.publishPerson).setOnClickListener(new View.OnClickListener() {
            String[] images=new String[]{"./uploadfile/2018-03-26/5f2a29b0-e351-4c45-9029-48902e9f3eeb_141256_employee.jpg",
                    "./uploadfile/2018-03-26/a9e68bf4-4b8f-4d3f-b689-df330488f056_133147_employee.jpg",
                    "./uploadfile/2018-03-29/f2043057-e072-4d84-b097-23f23190b71e_154326_employee.jpg"
            };
            int i=0;
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick: 识别" );
                MQTTService.publish("{\"category\":\"customer\"," +
                        "\"list\":{" +
                        "\"Id\":3," +
                        "\"Ename\":\"Seven Peng\"," +
                        "\"Name\":\"比尔\"," +
                        "\"Live\":\"Yes\"," +
                        "\"Gender\":\"Male\"," +
                        "\"Company\":\"SunOS\"," +
                        "\"Visitors\":1," +
                        "\"Purpose\":\"打算放大师傅\"," +
                        "\"Interviewer\":\"5\"," +
                        "\"Phone\":\"\"," +
                        "\"PhotoPath\":\""+images[(i++)%3]+"\"," +
                        "\"PdfPath\":\"\"," +
                        "\"Position\":\"PK\"," +
                        "\"Pubtime\":\"2018-03-30T02:43:30+08:00\"," +
                        "\"WelcomeMsg\":\""+(i%2==0?"":"欢迎来到达闼")+"\""+
                        "},\n" +
                        "\"status\":\"show\"" +
                        "}");
            }
        });
        findViewById(R.id.playVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: " );
                    Log.e(TAG, "onClick: 欢迎语" );
                    MQTTService.publish("{\"status\": \"welcome\", \"category\": \"welcome\", \"list\": \"欢迎金地集团某某某莅临指导！\"}");
//                playVideo();
            }
        });
        findViewById(R.id.set).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View setLayout = findViewById(R.id.setLayout);
                if(setLayout.getVisibility()==View.VISIBLE){
                    ((Button) v).setText("设置");
                    String ip = ((EditText) findViewById(R.id.ip)).getText().toString();
                    SharePreferenceUtils.setPrefString(Constant.IP_KEY,ip);
                    setLayout.setVisibility(View.GONE);

                }else{
                    ((Button) v).setText("保存");
                    setLayout.setVisibility(View.VISIBLE);
                    ((EditText) findViewById(R.id.ip)).setText(SharePreferenceUtils.getPrefString(Constant.IP_KEY,Constant.IP));
                }
            }
        });
        findViewById(R.id.dailyGuide_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MQTTService.publish("{\"status\": \"dynamics\", \"category\": \"dynamics\", \"list\": \"1.达闼科技中标中国移动达闼科技中标中国移动达闼科技中标中国移动！" +
                        "2.达闼科技中标中国移动达闼科技中标中国移动达闼科技中标中国移动！" +
                        "3.达闼科技中标中国移动达闼科技中标中国移动达闼科技中标中国移动！" +
                        "4.达闼科技中标中国移动达闼科技中标中国移动达闼科技中标中国移动！\"}");
            }
        });
    }

    //初始化控件
    private void initView() {
        recyclerView = ((RecyclerView) findViewById(R.id.recyclerView_personInfo));
        mainAdapter = new MainAdapter(this,new ArrayList<PersonInfoBean>());
        recyclerView.setAdapter(mainAdapter);
        mainBackground = ((ImageView) findViewById(R.id.main_bg));
        videoLayout = findViewById(R.id.videoLayout);
        mVideoView = (VideoView) findViewById(R.id.videoView);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnInfoListener(this);
        personInfoLayout = findViewById(R.id.personInfoLayout);
        show2 = findViewById(R.id.show2);
        welcomeLayout = findViewById(R.id.welcomeLayout);
        welcomeMsg = ((TextView) findViewById(R.id.welcome_msg));
        adTextView = ((ADTextView) findViewById(R.id.ad_textview));
        Utils.setFontType((CustomTextClock)findViewById(R.id.timeClock), "Roboto-Bold.ttf");
         List<String> texts = new ArrayList<>();
        texts.add("达闼科技携多款产品出战世界移动通信大会");
        texts.add("A2H 7/13上市，倒计时剩82天");
        texts.add("2018年4月24日达闼科技创始人兼CEO黄晓庆入选2017年中国知识产权领域最具影响力人物");
        adTextView.setInterval(8000);
        adTextView.init(texts,new OnAdChangeListener() {
            @Override
            public void DiyTextView(TextView textView) {
                if(textView.getTag()==null){
                    textView.setTag(true);
                    textView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                    textView.setTextSize(40);
                    textView.setTextColor(Color.WHITE);
                    textView.setGravity(Gravity.CENTER);
                    textView.setLineSpacing(0.0F, 1.1F);
                    Utils.setFontType(textView, "SourceHanSansCN-Bold.ttf");
                }
                String text = textView.getText().toString();
                String str1 = text.replaceAll("\\(|\\)", "");
                SpannableStringBuilder localSpannableStringBuilder = new SpannableStringBuilder(str1);
                Matcher matcher = Pattern.compile("\\(.+?\\)").matcher(text);
                int i = 0;
                while (matcher.find())
                {
                    int j = matcher.start() - i * 2;
                    String str2 = matcher.group();
                    int k = matcher.end() - (i + 1) * 2;
                    localSpannableStringBuilder.setSpan(new ForegroundColorSpan(Color.RED), j, k, 33);
                    LogUtils.d(MainActivity.TAG, "adTextView " + str2 + "  " + j + "  " + k + "  " + str1.substring(j, k));
                    i += 1;
                }
                textView.setText(localSpannableStringBuilder);
            }

        });

        mSurface = ((SurfaceView) findViewById(R.id.mSurface));
        hcClient = HCClient.initHCSDK(this,mSurface);
        showWelcome("啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊");
    }

    //初始化数据
    private void initData(){
        savePath=getInternalCacheDirectory(this,"mp4").getAbsolutePath();
        main_bg_url = SharePreferenceUtils.getPrefString(MAIN_BG_KEY,"");
        if(!TextUtils.isEmpty(main_bg_url)){
            changeMainBackground(main_bg_url);
        }
        usbBroadcastReceiver = new USBBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);//插
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);//拔
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);  //完全拔出
        intentFilter.addDataScheme("file");//没有这行监听不起作用
        registerReceiver(usbBroadcastReceiver, intentFilter);
        usbBroadcastReceiver.addUSBListener(this);

    }
    public void playVideoTest(final String path) {
        if(!new File(path).exists())return;
        Toast.makeText(MainActivity.this,path,Toast.LENGTH_LONG).show();
        setVertical(0);
        this.executorService.execute(new Runnable()
        {
            public void run()
            {
                MainActivity.this.mVideoView.suspend();
                Log.e(MainActivity.TAG, "playVideoTest: " + path);
                MainActivity.this.mVideoView.setVideoURI(Uri.parse(path));
                MainActivity.this.mVideoView.start();
            }
        });
    }


    public void showWelcome(String message){
//        setVertical(1);
//        StringBuffer sb=new StringBuffer();
//        int i=0;
//        for (i = 9; i < message.length(); i=i+9) {
//            sb.append(message.substring(i-9,i)+"\n");
//        }
//        if(i-9<message.length()) {
//            sb.append(message.substring(i - 9, message.length()));
//        }
//        Log.e(TAG, "showWelcome: "+sb.toString() );
//        welcomeMsg.setText(sb.toString());

        setVertical(1);
        String[] arrayOfString =message.split("\\*");
        String result = "";
        Log.e(TAG, "showDynamics: length = " + arrayOfString.length);
        int j = 0;
        for (j = 0; j <  arrayOfString.length - 1; j++) {
            String str = "" + arrayOfString[j].charAt(arrayOfString[j].length() - 1) + arrayOfString[(j + 1)].charAt(0);
            if ((str.matches("\\w{2}")) && (!Utils.isContainChinese(str))) {
                result+=arrayOfString[j] + "-\n";
            }else{
                result+=arrayOfString[j] + "\n";
            }

        }
        result = result + arrayOfString[(arrayOfString.length - 1)];
        Log.e(TAG, "showWelcome: " + result);
        if (Utils.isContainChinese(result))
        {
            this.welcomeMsg.setLineSpacing(0.0F, 1.25F);
            this.welcomeMsg.setTextSize(67.0F);
            Utils.setFontType(this.welcomeMsg, "SourceHanSansCN-Bold.ttf");
        }else{
            this.welcomeMsg.setLineSpacing(0.0F, 1.1F);
            this.welcomeMsg.setTextSize(67.0F);
            Utils.setFontType(this.welcomeMsg, "Roboto-Bold.ttf");
        }
        this.welcomeMsg.setText(result);
        return;
    }
    public void showDynamics(List<NewsBean.DataBean> list) {
        Log.e(TAG, "showDynamics: "+list.toString() );
        List<String> texts = new ArrayList<>();
        for (NewsBean.DataBean dataBean : list) {
            String[] arrayOfString = dataBean.getTitle().split("\\*");
            String result = "";
            Log.e(TAG, "showDynamics: length = " + arrayOfString.length);
            int j = 0;
            for (j = 0; j <  arrayOfString.length - 1; j++) {
                String str = "" + arrayOfString[j].charAt(arrayOfString[j].length() - 1) + arrayOfString[(j + 1)].charAt(0);
                if ((str.matches("\\w{2}")) && (!Utils.isContainChinese(str))) {
                    result+=arrayOfString[j] + "-\n";
                }else{
                    result+=arrayOfString[j] + "\n";
                }

            }
            texts.add(result + arrayOfString[(arrayOfString.length - 1)]);
        }
        adTextView.changeData(texts);
    }
    public void scanVideo()
    {
        if (!isDebug) {
            this.mHandler.sendEmptyMessageDelayed(103, 5000L);
        }
    }

    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    LinearLayoutManager linearManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    int firstItemPosition = linearManager.findFirstVisibleItemPosition();
                    int lastItemPosition = linearManager.findLastVisibleItemPosition();
                    int firstCompletelyVisibleItemPosition = linearManager.findFirstCompletelyVisibleItemPosition();
                    View view = linearManager.getChildAt(firstCompletelyVisibleItemPosition==-1?0:firstCompletelyVisibleItemPosition-firstItemPosition);
                    Log.e(TAG, "handleMessage: "+firstItemPosition +"   "+lastItemPosition+"   "+mainAdapter.getItemCount());
                    if(view!=null) {
                        Log.e(TAG, "onClick: " + view.getLeft() + "   " + view.getHeight() + "   " + (view.getHeight() + view.getTop()));
                        recyclerView.smoothScrollBy(view.getLeft(), view.getHeight() + view.getTop());
                    }
                    if(mainAdapter.getItemCount()-firstItemPosition>3) {
                        mHandler.sendEmptyMessageDelayed(1, mainAdapter.getItemCount() -firstItemPosition> 8 ? 1500 : mainAdapter.getItemCount()-firstItemPosition > 6 ? 2000 : 3000);
                    }else{
                        mainAdapter.clear();
                    }
                    break;
                case 100:
                    Log.e(TAG, "handleMessage: "+DateUtil.stringForTime(mVideoView.getCurrentPosition())+"  "+DateUtil.stringForTime(Integer.valueOf(mVideoView.getDuration())));
                    mHandler.sendEmptyMessageDelayed(100,1000);
                    break;
                case 101:
                    Log.e(TAG, "onMeasure: "+mVideoView.getWidth()+"  "+mVideoView.getHeight() );
//                    show2.setScrollX(100);
//                    ViewGroup.LayoutParams params = new RelativeLayout.LayoutParams((int)(mVideoView.getWidth()*1.3f),(int)(mVideoView.getHeight()*1.3f));
//                    mVideoView.setLayoutParams(params);
//                    mVideoView.requestLayout();
                    Log.e(TAG, "onMeasure: "+mVideoView.getWidth()+"  "+mVideoView.getHeight() );
                    break;
                case 102:
                    Log.e(TAG, "handleMessage: "+102 );
                    if(hcClient.loginNormalDevice()==-1){
                        mHandler.sendEmptyMessageDelayed(102,5000);
                        Toast.makeText(MainActivity.this, "连接摄像头失败", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(MainActivity.this, "连接摄像头成功", Toast.LENGTH_SHORT).show();
                        hcClient.preview();
                    }
                    break;
                case 103:
                    File file = new File("/mnt/usb");
                    if (!((File)file).exists()) {
                        file = new File("/mnt/sda");
                    }
                    File[] files = file.listFiles();
                    if (files != null&&files.length>0) {
                        ToastUtil.show(MainActivity.this, "正在扫描U盘", MainActivity.isDebug);
                        for (File file1 : files) {
                            MainActivity.this.pullIn(file1.getAbsolutePath());
                        }
                    }else {
                        MainActivity.this.mHandler.sendEmptyMessageDelayed(103, 5000L);
                        Toast.makeText(MainActivity.this, "U盘未插入", Toast.LENGTH_SHORT).show();
                    }
                    break;

            }
        }
    };
    public void showPersonInfo(PersonInfoBean employeeBean){
        if (mainAdapter.getItemCount() == 0) {
            mainAdapter.add(new PersonInfoBean(false));
            mainAdapter.add(new PersonInfoBean(false));
            mainAdapter.add(new PersonInfoBean(false));
            mainAdapter.add(new PersonInfoBean(false));
        }
        mainAdapter.add(employeeBean, mainAdapter.getItemCount() - 3);
        if (!mHandler.hasMessages(1)) {
            mHandler.sendEmptyMessageDelayed(1, mainAdapter.getItemCount() > 8 ? 1500 : mainAdapter.getItemCount() > 8 ? 2000 : 3000);
        }
    }
    int i=0;
    public void changeMainBackground(String imageUrl){
        Log.e(TAG, "changeMainBackground: "+imageUrl );
        Glide.with(this)
                .load(Constant.getImagebaseUrl() +imageUrl)
                .diskCacheStrategy( DiskCacheStrategy.SOURCE )
                .into(mainBackground);
    }
    public void setVertical(int type){
        Log.e(TAG, "setVertical: "+type+"  "+this.type  );
        if(this.type == type)return;
        this.type = type;
        switch (type){
            case 0:
                videoLayout.setVisibility(View.VISIBLE);
                welcomeLayout.setVisibility(View.GONE);
                break;
            case 1:
                Log.e(TAG, "setVertical: isPlaying = "+mVideoView.isPlaying() );
                if(mHandler.hasMessages(100)){
                    mHandler.removeMessages(100);
                }
                if(mVideoView.isPlaying()){
                    mVideoView.suspend();//将VideoView所占用的资源释放掉
                }
                videoLayout.setVisibility(View.GONE);
                welcomeLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        Log.e(TAG, "onClick: " );
        switch (v.getId()){
            case R.id.videoLayout:
                break;
        }
    }
    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.e(TAG, "onPrepared: " );
        if(!mHandler.hasMessages(100))
        mHandler.sendEmptyMessageDelayed(100,0);
//        mp.start();
//        mp.setLooping(true);
    }
    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "onInfo: " +what+ "  "+extra);
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                }
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                mVideoView.start();
                break;
            default:
                Log.e(TAG, "onInfo:  what = "+what+"  extra = "+extra );
                break;
        }
        return true;
    }
    @Override
    public void error(String msg) {
        Log.e(TAG, "error: "+msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.e(TAG, "onCompletion: " );
        if(mHandler.hasMessages(100)){
            mHandler.removeMessages(100);
        }
        playVideoTest(videoPaths[playVideoIndex%videoPaths.length].getAbsolutePath());
        playVideoIndex++;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "onError:  what = "+what+"  extra = "+extra  );
        if(mHandler.hasMessages(100)){
            mHandler.removeMessages(100);
        }
        return false;
    }
    protected void onPause()
    {
        super.onPause();
        this.hcClient.stopSinglePreview();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        persenter.getAllNews("/screennews/getnews");
//        playVideoTest("");
        this.persenter.getAllNews("/screennews/getnews");
        mHandler.sendEmptyMessageDelayed(102,3000);
        scanVideo();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //屏幕切换时，设置全屏
        super.onConfigurationChanged(newConfig);
    }
    @Override
    protected void onDestroy() {
        persenter.destory();
        hcClient.stopPlayBack();
        if(mHandler.hasMessages(1))
        mHandler.removeMessages(1);
        if(mHandler.hasMessages(100))
            mHandler.removeMessages(100);
        if(mHandler.hasMessages(101))
            mHandler.removeMessages(101);
        if(mHandler.hasMessages(102))
            mHandler.removeMessages(102);
        super.onDestroy();
        unregisterReceiver(usbBroadcastReceiver);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void pullIn(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        for (File file1 : files) {
            if(file1.getName().equals("达闼宣传片")){
                videoPaths = file1.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        String s = name.toLowerCase();
                        return s.endsWith(".mp4")|| s.endsWith(".mov");
                    }
                });
                if(videoPaths!=null&&videoPaths.length>0) {
                    playVideoIndex=0;
                    playVideoTest(videoPaths[playVideoIndex++].getAbsolutePath());
                }
                break;
            }
        }

    }

    @Override
    public void pullOut() {
        playVideoIndex=-1;
        mVideoView.suspend();
    }
}
