package com.example.tvscreeninfo.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by WQ on 2018/4/22.
 */

public class Utils {
    private static HashMap<String, Typeface> tfs = new HashMap();

    public static void init(final Context paramContext)
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                AssetManager localObject = paramContext.getAssets();
                Typeface localTypeface = Typeface.createFromAsset(localObject, "SourceHanSansCN-Bold.ttf");
                Utils.tfs.put("SourceHanSansCN-Bold.ttf", localTypeface);
                localTypeface = Typeface.createFromAsset(localObject, "SourceHanSansCN-Heavy.ttf");
                Utils.tfs.put("SourceHanSansCN-Heavy.ttf", localTypeface);
                localTypeface = Typeface.createFromAsset(localObject, "Roboto-Medium.ttf");
                Utils.tfs.put("Roboto-Medium.ttf", localTypeface);
                localTypeface = Typeface.createFromAsset(localObject, "Roboto-Regular.ttf");
                Utils.tfs.put("Roboto-Regular.ttf", localTypeface);
                localTypeface = Typeface.createFromAsset(localObject, "Roboto-Bold.ttf");
                Utils.tfs.put("Roboto-Bold.ttf", localTypeface);
            }
        }).start();
    }

    public static boolean isContainChinese(String paramString)
    {
        return Pattern.compile("[\\u4e00-\\u9fa5]").matcher(paramString).find();
    }

    public static void setFontType(TextView paramTextView, String paramString)
    {
        if (tfs.containsKey(paramString))
        {
            paramTextView.setTypeface(tfs.get(paramString));
            return;
        }
        Toast.makeText(paramTextView.getContext(), paramString + "字体未加载", Toast.LENGTH_SHORT).show();
    }
}
