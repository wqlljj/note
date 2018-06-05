package com.cloudminds.register.ui.view;

import android.content.Context;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomAutoCompleteTextView
        extends AppCompatAutoCompleteTextView
{
    private String TAG = "CustomAutoCompleteTextView";

    public CustomAutoCompleteTextView(Context paramContext)
    {
        super(paramContext);
    }

    public CustomAutoCompleteTextView(Context paramContext, AttributeSet paramAttributeSet)
    {
        super(paramContext, paramAttributeSet);
    }

    public CustomAutoCompleteTextView(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
    {
        super(paramContext, paramAttributeSet, paramInt);
    }

    public static boolean isContainChinese(String paramString)
    {
        return Pattern.compile("[\\u4e00-\\u9fa5]").matcher(paramString).find();
    }

    public boolean enoughToFilter()
    {
        boolean flag=false;
        if (isContainChinese(getText().toString())) {
            flag = true;
        }else {
            flag = getText().length() >=2 ? true : false;
        }
        Log.e(TAG, "enoughToFilter: "+flag);
        return flag;
    }

    public int getThreshold()
    {
        Log.e(this.TAG, "getThreshold: " + super.getThreshold());
        return super.getThreshold();
    }
}
