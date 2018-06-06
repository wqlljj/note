package com.cloudminds.meta.view;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;

import com.cloudminds.meta.R;

/**
 * Created by tiger on 17-3-31.
 */

public class EditTextWithDel extends EditText {

    private final static String TAG = "Meta:EditTextWithDel";

    private Drawable img;
    private Context mContext;

    public EditTextWithDel(Context context) {
        super(context);
        this.mContext =context;
        init();
    }

    public EditTextWithDel(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext =context;
        init();
    }

    public EditTextWithDel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext =context;
        init();
    }


    private void init() {
        img = mContext.getResources().getDrawable(R.drawable.ic_search_delete);
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                setDrawable();
            }
        });
        setDrawable();
    }

    private void setDrawable() {
        if(length()<1){
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }else {
            setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (img != null && event.getAction() == MotionEvent.ACTION_UP) {
            int eventX = (int) event.getRawX();
            int eventY = (int) event.getRawY();
            Log.e(TAG, "eventX = " + eventX + "; eventY = " + eventY);
            Rect rect = new Rect();
            getGlobalVisibleRect(rect);
            rect.left = rect.right - 100;
            if(rect.contains(eventX, eventY))
                setText("");
        }
        return super.onTouchEvent(event);
    }
}
