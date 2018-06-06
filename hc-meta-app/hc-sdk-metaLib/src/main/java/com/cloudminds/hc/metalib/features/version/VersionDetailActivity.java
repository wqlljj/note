package com.cloudminds.hc.metalib.features.version;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.cloudminds.hc.metalib.R;
import com.cloudminds.hc.metalib.features.BaseActivity;


public class VersionDetailActivity extends BaseActivity {

    public static final String INTENT_EXTRA_DETAIL = "INTENT_EXTRA_DETAIL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version_detail);
        Intent intent = getIntent();
        String detail;
        if (null == intent || !intent.hasExtra(INTENT_EXTRA_DETAIL) || TextUtils.isEmpty(detail = intent.getStringExtra(INTENT_EXTRA_DETAIL))) {
            finish();
            return;
        }
        TextView versionDetail = v(R.id.versionDetail);
        versionDetail.setText(detail);
    }
}
