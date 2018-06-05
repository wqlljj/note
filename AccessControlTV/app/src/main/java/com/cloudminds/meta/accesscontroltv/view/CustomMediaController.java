package com.cloudminds.meta.accesscontroltv.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.MediaController;

/**
 * Created by WQ on 2018/4/20.
 */

public class CustomMediaController extends MediaController {
    public CustomMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomMediaController(Context context, boolean useFastForward) {
        super(context, useFastForward);
    }

    public CustomMediaController(Context context) {
        super(context);
    }

    @Override
    public void show() {
        super.show();
    }
}
