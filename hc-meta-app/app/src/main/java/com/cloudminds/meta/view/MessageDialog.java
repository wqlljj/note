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

public class MessageDialog {

    private int mTitle;
    private int mMessage;
    private int mUnbind;
    private View.OnClickListener mOkListener;
    private AlertDialog mDialog;
    private int mCancle;
    private View.OnClickListener mCancleListener;

    public MessageDialog(MessageDialog.Builder build){
        this.mTitle = build.mTitle;
        this.mUnbind = build.mOk;
        this.mOkListener = build.mOkListener;
        this.mCancle = build.mCancle;
        this.mCancleListener = build.mCancleListener;
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
        window.setContentView(R.layout.equipment_dialog);

        TextView titleTv = (TextView) window.findViewById(R.id.equipment_dialog_title);
        titleTv.setText(getString(context,mTitle));
        TextView messageTv = (TextView) window.findViewById(R.id.equipment_dialog_message);
        messageTv.setText(getString(context,mMessage));

        TextView cancle = (TextView) window.findViewById(R.id.equipment_dialog_cancle);
        if(mCancleListener != null){
            cancle.setOnClickListener(mCancleListener);
        }
        cancle.setText(getString(context,mCancle));

        TextView okTv = (TextView) window.findViewById(R.id.equipment_dialog_ok);
        if(mOkListener!=null)
            okTv.setOnClickListener(mOkListener);
        okTv.setText(getString(context,mUnbind));
    }

    public void dismiss(){
        if(mDialog!=null){
            mDialog.dismiss();
        }
    }

    public static class Builder{
        public int mTitle;
        public int mOk;
        public int mCancle;
        public View.OnClickListener mCancleListener;
        public View.OnClickListener mOkListener;

        public MessageDialog.Builder setMessage(int mMessage) {
            this.mMessage = mMessage;
            return this;
        }

        public int mMessage;

        public MessageDialog.Builder setCancle(int mCancle) {
            this.mCancle = mCancle;
            return this;
        }

        public MessageDialog.Builder setCancleListener(View.OnClickListener mCancleListener) {
            this.mCancleListener = mCancleListener;
            return this;
        }

        public MessageDialog.Builder setTitle(int mTitle) {
            this.mTitle = mTitle;
            return this;
        }

        public MessageDialog.Builder setOk(int mOk) {
            this.mOk = mOk;
            return this;
        }

        public MessageDialog.Builder setOkListener(View.OnClickListener mOkListener) {
            this.mOkListener = mOkListener;
            return this;
        }

        public MessageDialog builder(){
            return new MessageDialog(this);
        }

    }

}
