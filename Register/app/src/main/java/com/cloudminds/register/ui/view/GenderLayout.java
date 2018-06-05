package com.cloudminds.register.ui.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cloudminds.register.R;
import com.cloudminds.register.utils.Utils;

/**
 * Created
 */

public class GenderLayout extends LinearLayout implements View.OnClickListener {

    private ImageView mMale;
    private ImageView mFamale;

    public GenderLayout(Context context) {
        super(context);
    }

    public GenderLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GenderLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mMale = findViewById(R.id.iv_male);
        mFamale = findViewById(R.id.iv_famale);
        mMale.setOnClickListener(this);
        mFamale.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_male:
                setMale();
                break;
            case R.id.iv_famale:
                setFamale();
                break;
            default:
                setMale();
                break;
        }
    }

    private void setMale() {
        mMale.setSelected(true);
        mFamale.setSelected(false);
    }

    private void setFamale() {
        mMale.setSelected(false);
        mFamale.setSelected(true);
    }

    public String getGender() {
        int result;
        if (mMale.isSelected()) {
            result = 0;
        } else {
            result = 1;
        }
        return Utils.convertGender(result);
    }

    public int getGenderToInt() {
        int result;
        if (mMale.isSelected()) {
            result = 0;
        } else {
            result = 1;
        }
        return result;
    }

    public void setGender(int gender) {
        switch (gender) {
            case 0:
                setMale();
                break;
            case 1:
                setFamale();
                break;
            default:
                setMale();
                break;
        }
    }

}
