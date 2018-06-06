package com.cloudminds.meta.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by tiger on 17-3-31.
 */

public class GuideAdapter extends PagerAdapter {

    private int[] mTips;
    private Context mContext;

    public GuideAdapter(Context mContext, int[] mTips){
        this.mTips = mTips;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mTips.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView im=new ImageView(mContext);
        im.setImageResource(mTips[position]);
        container.addView(im);
        return im;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

    }
}
