package com.cloudminds.register.ui.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cloudminds.register.R;

/**
 * Created
 */

public class DateLinearLayout extends LinearLayout implements View.OnClickListener {

    private TextView mYearTV;
    private TextView mMonthTV;
    private TextView mDayTV;
    private LinearLayout mDateLayout;
    private Context mContext;

    public DateLinearLayout(Context context) {
        super(context);
        mContext = context;
    }

    public DateLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public DateLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    @Override
    public void onClick(View v) {

    }
}
