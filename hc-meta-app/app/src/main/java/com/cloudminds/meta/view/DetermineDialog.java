package com.cloudminds.meta.view;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.cloudminds.meta.R;

/**
 * Created by tiger on 17-4-5.
 */

public class DetermineDialog {

    private int mTitle;
    private int mOk;
    private View.OnClickListener mOkListener;
    private AlertDialog mDialog;
    private String mMessage;

    public DetermineDialog(Builder build){
        this.mTitle = build.mTitle;
        this.mOk = build.mOk;
        this.mOkListener = build.mOkListener;
        this.mMessage = build.mMessage;
    }

    private String getString(Context context,int resources){
        return context.getString(resources);
    }

    public void show(Context context){
        mDialog = new AlertDialog.Builder(context).create();
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
        Window window = mDialog.getWindow();
//        window.setGravity(Gravity.BOTTOM);
        window.setContentView(R.layout.activate_dialog);

        TextView titleTv = (TextView) window.findViewById(R.id.prop_title);
        if(mMessage!=null){
            titleTv.setText(mMessage);
        }else {
            titleTv.setText(getString(context,mTitle));
        }
        TextView okTv = (TextView) window.findViewById(R.id.prop_ok);
        if(mOkListener!=null)
            okTv.setOnClickListener(mOkListener);
        okTv.setText(getString(context,mOk));
    }

    public void dismiss(){
        if(mDialog!=null){
            mDialog.dismiss();
        }
    }

    public static class Builder{
        public int mTitle;
        public int mOk;

        public DetermineDialog.Builder setMessage(String mMessage) {
            this.mMessage = mMessage;
            return this;
        }

        public String mMessage;

        private View.OnClickListener mOkListener;

        public DetermineDialog.Builder setTitle(int mTitle) {
            this.mTitle = mTitle;
            return this;
        }

        public DetermineDialog.Builder setOk(int mOk) {
            this.mOk = mOk;
            return this;
        }

        public DetermineDialog.Builder setOkListener(View.OnClickListener mOkListener) {
            this.mOkListener = mOkListener;
            return this;
        }

        public DetermineDialog builder(){
            return new DetermineDialog(this);
        }

    }

}
