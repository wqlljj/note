package com.cloudminds.meta.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.cloudminds.meta.activity.FamilyManageActivity;

import java.lang.ref.WeakReference;

/**
 * Created by tiger on 17-4-12.
 */

public class BaseFamilyFragment extends Fragment{

//    public WeakReference<FamilyManageActivity> mActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        mActivity = new WeakReference<FamilyManageActivity>((FamilyManageActivity)context );
    }

    public void release(){
//        mActivity=null;
    }
}
