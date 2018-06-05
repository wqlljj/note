package com.cloudminds.meta.accesscontroltv.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import java.util.Calendar;
import java.util.TimeZone;

public class CustomTextClock
        extends AppCompatTextView
{
    private String TAG = "CustomTextClock";
    private boolean mAttached;
    String mFormat = "yyyy年MM月dd日  HH:mm:ss";
    private boolean mHasSeconds;
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context paramAnonymousContext, Intent paramAnonymousIntent)
        {
            if ((CustomTextClock.this.mTimeZone == null) && ("android.intent.action.TIMEZONE_CHANGED".equals(paramAnonymousIntent.getAction())))
            {
                String s = paramAnonymousIntent.getStringExtra("time-zone");
                CustomTextClock.this.createTime(s);
            }
            CustomTextClock.this.onTimeChanged();
        }
    };
    private final Runnable mTicker = new Runnable()
    {
        public void run()
        {
            CustomTextClock.this.onTimeChanged();
            long l = SystemClock.uptimeMillis();
            CustomTextClock.this.getHandler().postAtTime(CustomTextClock.this.mTicker, l + (1000L - l % 1000L));
        }
    };
    private Calendar mTime;
    private String mTimeZone;

    public CustomTextClock(Context paramContext)
    {
        super(paramContext);
    }

    public CustomTextClock(Context paramContext, @Nullable AttributeSet paramAttributeSet)
    {
        super(paramContext, paramAttributeSet);
    }

    public CustomTextClock(Context paramContext, @Nullable AttributeSet paramAttributeSet, int paramInt)
    {
        super(paramContext, paramAttributeSet, paramInt);
        init();
    }

    private void createTime(String paramString)
    {
        if (paramString != null)
        {
            this.mTime = Calendar.getInstance(TimeZone.getTimeZone(paramString));
            return;
        }
        this.mTime = Calendar.getInstance();
    }

    private void init()
    {
        this.mHasSeconds = this.mFormat.contains("ss");
        Log.e(this.TAG, "init: mHasSeconds = " + this.mHasSeconds);
        createTime(this.mTimeZone);
    }

    private void onTimeChanged()
    {
        this.mTime.setTimeInMillis(System.currentTimeMillis());
        setText(DateFormat.format(this.mFormat, this.mTime));
        setContentDescription(DateFormat.format(this.mFormat, this.mTime));
    }

    private void registerReceiver()
    {
        IntentFilter localIntentFilter = new IntentFilter();
        localIntentFilter.addAction("android.intent.action.TIME_TICK");
        localIntentFilter.addAction("android.intent.action.TIME_SET");
        localIntentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
        getContext().registerReceiver(this.mIntentReceiver, localIntentFilter, null, getHandler());
    }

    private void unregisterReceiver()
    {
        getContext().unregisterReceiver(this.mIntentReceiver);
    }

    public String getmFormat()
    {
        return this.mFormat;
    }

    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        if (!this.mAttached)
        {
            this.mAttached = true;
            registerReceiver();
            createTime(this.mTimeZone);
            this.mHasSeconds = this.mFormat.contains("ss");
            Log.e(this.TAG, "onAttachedToWindow: mHasSeconds = " + this.mHasSeconds);
            if (this.mHasSeconds) {
                this.mTicker.run();
            }
        }
        else
        {
            return;
        }
        onTimeChanged();
    }

    protected void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        if (this.mAttached)
        {
            unregisterReceiver();
            getHandler().removeCallbacks(this.mTicker);
            this.mAttached = false;
        }
    }

    public void setmFormat(String paramString)
    {
        this.mFormat = paramString;
        createTime(this.mTimeZone);
        onTimeChanged();
    }
}
