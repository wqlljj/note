package com.cloudminds.meta.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;

import com.cloudminds.meta.R;

/**
 * Created by tiger on 17-4-1.
 */

public class TakePhotosDialog extends Dialog{

    public TakePhotosDialog(@NonNull Context context) {
        super(context);
        this.show();
    }

    public TakePhotosDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        this.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.takephotos_dialog);
    }
}
