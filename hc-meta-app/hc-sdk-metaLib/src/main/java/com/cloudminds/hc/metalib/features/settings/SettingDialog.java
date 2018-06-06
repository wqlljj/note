package com.cloudminds.hc.metalib.features.settings;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.cloudminds.hc.metalib.R;
import com.cloudminds.hc.metalib.config.Config;
import com.cloudminds.hc.metalib.utils.DLog;


/**
 * Created by willzhang on 29/06/17
 */

public class SettingDialog extends Dialog implements CompoundButton.OnCheckedChangeListener {

    private View anchorView;

    public SettingDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.menu_auto_download_wifi);
        CheckBox checkBox = (CheckBox) findViewById(R.id.auto_download);
        checkBox.setChecked(Config.getInstance().isAutoDownloadOnWifi(context));
        checkBox.setOnCheckedChangeListener(this);
    }

    public void setAnchorView(View anchorView) {
        this.anchorView = anchorView;
    }

    @Override
    public void show() {
        super.show();
        Window window = getWindow();
        if (null != window) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.TOP | Gravity.END;
            if (null != anchorView) {
                lp.x = anchorView.getWidth() / 2;
                lp.y = anchorView.getHeight() / 2;
            }
            window.setAttributes(lp);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        DLog.d("auto download on wifi:" + isChecked);
        Config.getInstance().setAutoDownloadOnWifi(getContext(), isChecked);
    }
}
