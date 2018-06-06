package com.cloudminds.hc.metalib.features.test;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.cloudminds.hc.metalib.R;
import com.cloudminds.hc.metalib.config.Config;
import com.cloudminds.hc.metalib.features.BaseActivity;
import com.cloudminds.hc.metalib.utils.ToastUtil;


public class TestActivity extends BaseActivity implements View.OnClickListener {

    private TextView version;
    private EditText serverURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Switch versionSwitcher = v(R.id.switchVersion);
        boolean versionTest = Config.getInstance().isVersionTest(this);
        versionSwitcher.setChecked(versionTest);
        versionSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Config.getInstance().setVersionTest(TestActivity.this, isChecked);
                setVersion(isChecked);
            }
        });
        version = v(R.id.current_version);
        setVersion(versionTest);
        Button buttonOK = v(R.id.bt_addr_commit);
        buttonOK.setOnClickListener(this);
        serverURL = v(R.id.et_addr_input);
    }

    private void setVersion(boolean versionTest) {
        if (versionTest) {
            String forceVersion = getString(R.string.show_version_list);
            version.setText(forceVersion);
        } else {
            version.setText(getString(R.string.do_not_show_version_list));
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id==R.id.bt_addr_commit){
                String url = serverURL.getText().toString();
                if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(url.trim())) {
                    Config.getInstance().setCheckUrl(this, url);
                } else {
                    ToastUtil.show(this.getApplicationContext(), R.string.host_can_not_empty);
                }
        }
    }
}
