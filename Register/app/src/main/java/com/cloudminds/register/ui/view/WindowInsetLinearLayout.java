package com.cloudminds.register.ui.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.WindowInsets;
import android.widget.LinearLayout;

/**
 * 1. Modify "android:fitsSystemWindows" ,"full screen" and Input.
 */

public class WindowInsetLinearLayout extends LinearLayout {

    private int[] mInsets = new int[4];


    public WindowInsetLinearLayout(Context context) {
        super(context);
    }

    public WindowInsetLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public WindowInsetLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public final WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            mInsets[0] = insets.getSystemWindowInsetLeft();
            mInsets[1] = insets.getSystemWindowInsetTop();
            mInsets[2] = insets.getSystemWindowInsetRight();
            return super.onApplyWindowInsets(insets.replaceSystemWindowInsets(0, 0, 0,
                    insets.getSystemWindowInsetBottom()));
        } else {
            return insets;
        }
    }

}
