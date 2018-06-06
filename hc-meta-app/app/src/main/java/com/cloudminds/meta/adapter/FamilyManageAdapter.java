package com.cloudminds.meta.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.cloudminds.hc.cloudService.api.HCApiClient;
import com.cloudminds.hc.cloudService.http.HCBaseHttp;
import com.cloudminds.hc.metalib.utils.ToastUtil;
import com.cloudminds.meta.R;
import com.cloudminds.meta.bean.FamilyItemBean;
import com.cloudminds.meta.util.FileUtils;
import com.getui.logful.util.LogUtil;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiger on 17-4-11.
 */

public class FamilyManageAdapter extends BaseAdapter{
    private List<FamilyItemBean> mItems;

    public FamilyManageAdapter(List<FamilyItemBean> items){
        this.mItems = items;
    }
    public void release() {
        mItems.clear();
        notifyDataSetChanged();
        mItems=null;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
    int num=0;
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final ViewHolder holder;
        if(view == null){
            Log.e("inflate", "getView: "+num++ );
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.family_manage_item,null);
            holder = new ViewHolder();
            holder.imageView = (ImageView) view.findViewById(R.id.family_manage_item_image);
            holder.textView = (TextView) view.findViewById(R.id.family_manage_item_name);
            view.setTag(holder);
        }else {
            holder = (ViewHolder) view.getTag();
        }
        if(holder.imageView != null && holder.imageView.getDrawable() != null){
            Bitmap oldBitmap = ((BitmapDrawable) holder.imageView.getDrawable()).getBitmap();
            holder.imageView.setImageDrawable(null);
//            if(oldBitmap != null){
//                oldBitmap.recycle();
//                oldBitmap = null;
//            }
        }

        if(i < mItems.size()){
            final FamilyItemBean item = mItems.get(i);
            holder.imageView.setImageResource(R.mipmap.loading);
            Animation rotateAnimation  = new RotateAnimation(0, 359, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setFillAfter(true);
            rotateAnimation.setDuration(5000);
            rotateAnimation.setRepeatCount(-1);
            rotateAnimation.setInterpolator(new LinearInterpolator());
            holder.imageView.startAnimation(rotateAnimation);
            setImage(holder.imageView, item,rotateAnimation,0);
            holder.textView.setText(item.getName());
        }
        return view;
    }

    private void setImage(final ImageView imageView, final FamilyItemBean item,final Animation anim,final int try_num) {
        LogUtil.e("inflate", "setImage: " +item.toString()+"  "+try_num);
        File file = new File(item.getImageLocalPath());
        if(!file.exists()){

            HCApiClient.downloadPicFromNet(item.getImageNetPath().replace("39.155.168.50","10.11.35.201"), item.getImageLocalPath(), new HCBaseHttp.CallBack<String>() {
                @Override
                public void onResponse(final String data) {
                    LogUtil.e("inflate", "onResponse:1 " );

                    imageView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(data.endsWith("success")){
                                LogUtil.e("inflate", "onResponse: 2" );
                                anim.cancel();
                                FileUtils.insertImageToGallery(imageView.getContext(),item.getImageLocalPath());
                                imageView.setImageBitmap(BitmapFactory.decodeFile(item.getImageLocalPath()));
                            }else{
                                if(try_num>2) {
                                    LogUtil.e("inflate", "onResponse: 3" );
                                    anim.cancel();
                                    imageView.setImageResource(R.mipmap.load_fail);
                                    ToastUtil.show(imageView.getContext(), data);
                                }else{
                                    LogUtil.e("inflate", "onResponse: 4" );
                                    setImage(imageView,item,anim,try_num+1);
                                }
                            }
                        }
                    },1000);
                }

                @Override
                public void onFailure(final String msg) {
                    LogUtil.e("inflate", "onResponse:5 " );
                    imageView.post(new Runnable() {
                        @Override
                        public void run() {
                            if(try_num>2) {
                                LogUtil.e("inflate", "onResponse: 6" );
                                anim.cancel();
                                imageView.setImageResource(R.mipmap.load_fail);
                                ToastUtil.show(imageView.getContext(), msg);
                            }else{
                                LogUtil.e("inflate", "onResponse: 7" );
                                setImage(imageView,item,anim,try_num+1);
                            }
                        }
                    });
                }
            });
        }else {
            LogUtil.e("inflate", "onResponse: " );
            anim.cancel();
            try {
                LogUtil.e("inflate", "onResponse: " );
                imageView.setImageBitmap(scaleImage(item.getImageLocalPath()));
            }catch (Exception e){
                LogUtil.e("Test", "getView: "+item.getImageLocalPath()+"\n"+e.getMessage());
                ToastUtil.show(imageView.getContext(), R.string.iamge_load_fail);
            }
        }
    }

    private Bitmap scaleImage(String path){
        try {
            FileInputStream fis = new FileInputStream(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fis.getFD(), null, options);
            float srcWidth = options.outWidth;
            float srcHeight = options.outHeight;
            int inSampleSize = 1;

            if (srcHeight > 150 || srcWidth > 120) {
                if (srcWidth > srcHeight) {
                    inSampleSize = Math.round(srcHeight / 150);
                } else {
                    inSampleSize = Math.round(srcWidth / 120);
                }
            }

            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;
            options.inPreferredConfig= Bitmap.Config.RGB_565;

            return BitmapFactory.decodeFileDescriptor(fis.getFD(), null, options);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;

    }

    class ViewHolder{
        public ImageView imageView;
        public TextView textView;
    }
}
