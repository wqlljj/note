package com.cloudminds.meta.fragment;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.cloudminds.hc.cloudService.api.HCApiClient;
import com.cloudminds.hc.cloudService.bean.FaceBean;
import com.cloudminds.hc.cloudService.bean.FaceResponse;
import com.cloudminds.hc.cloudService.http.HCBaseHttp;
import com.cloudminds.hc.metalib.utils.ToastUtil;
import com.cloudminds.meta.R;
import com.cloudminds.meta.activity.FamilyManageActivity;
import com.cloudminds.meta.adapter.FamilyManageAdapter;
import com.cloudminds.meta.bean.FamilyItemBean;
import com.cloudminds.meta.constant.Constant;
import com.cloudminds.meta.greendao.DBManager;
import com.cloudminds.meta.util.FileUtils;
import com.getui.logful.util.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by tiger on 17-4-12.
 */

public class FamilyManageFragment extends BaseFamilyFragment
        implements View.OnClickListener, AdapterView.OnItemClickListener {

    public static final String TAG = "FamilyManageFragment";
    private GridView mFamilyManage;
    private ImageView mBack,mAdd;
    private ImageView mFresh;
    private FamilyManageAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDatas();
    }

    private void initDatas() {
        FamilyManageActivity activity = (FamilyManageActivity) getActivity();
        if(activity.mItems != null){
            Log.d(TAG,"datas has init ,so return!");
            return;
        }
        activity.mItems =DBManager.getInstance(getContext()).queryFamilyItemBeanList();
        int i=0;
        for (FamilyItemBean mItem : activity.mItems) {
            Log.e(TAG, (i++)+"  initDatas: "+mItem.toString() );
        }
        if(activity.mItems==null){
            activity.mItems=new ArrayList<>();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.family_manage_fragment,container,false);
        mFamilyManage = (GridView) view.findViewById(R.id.family_manage_gv);
        mBack = (ImageView) view.findViewById(R.id.family_manage_back);
        mAdd = (ImageView) view.findViewById(R.id.family_manage_add);
        mFresh = (ImageView) view.findViewById(R.id.family_manage_fresh);
        mBack.setOnClickListener(this);
        mAdd.setOnClickListener(this);
        mFresh.setOnClickListener(this);
         adapter = new FamilyManageAdapter(((FamilyManageActivity) getActivity()).mItems);
        mFamilyManage.setAdapter(adapter);
        mFamilyManage.setOnItemClickListener(this);
        return view;
    }

    @IdRes
    int btId;
    long clickTime=0l;
    private boolean checkReclick(@IdRes int id,long timeLimit) {
        Log.e(TAG, "checkReclick: "+id+"  "+timeLimit );
        if(btId==id&& System.currentTimeMillis()-clickTime<timeLimit){
            ToastUtil.show(getActivity().getApplicationContext(), R.string.repetitive_operation);
            Log.e(TAG, "checkReclick: true" );
            return true;
        }
        Log.e(TAG, "checkReclick: false" );
        btId=id;
        clickTime=System.currentTimeMillis();
        return false;
    }
    @Override
    public void onClick(View view) {
        final FamilyManageActivity activity = (FamilyManageActivity) getActivity();
        switch (view.getId()){
            case R.id.family_manage_add:
                if(checkReclick(R.id.family_manage_add,1000))return;
                activity.mHandler.sendEmptyMessage(Constant.FAMILY_DO_ADD);
                break;
            case R.id.family_manage_fresh:
                mFresh.setEnabled(false);
                ToastUtil.show(activity.getApplicationContext(), R.string.synchronizing_cloud_data);
                HCApiClient.getFaceList(new HCBaseHttp.CallBack<FaceResponse>() {
                    @Override
                    public void onResponse(final FaceResponse data) {
                        if(data.getCode()==0){
//                            DBManager.getInstance(getContext()).insertFamilyItemBean(mBean);
                            if(getActivity()!=null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
//                                        Log.e(TAG, "同步成功 :" + data.toString());
                                        ToastUtil.show(activity.getApplicationContext(), R.string.synchronization_success);
                                        List<FaceBean> faces = data.getFaces();
                                        activity.mItems.clear();
                                        for (FaceBean face : faces) {
                                            LogUtil.e(TAG, "run: "+face.toString());
                                            FamilyItemBean familyItemBean = new FamilyItemBean();
                                            familyItemBean.setFace_id(face.getFace_id());
                                            familyItemBean.setName(face.getName());
                                            String image = face.getImage();
                                            String imageName = image.substring(image.lastIndexOf("/") + 1);
                                            familyItemBean.setImageLocalPath(FamilyAddOrUpdateFragment.ImagePath + imageName);
                                            familyItemBean.setImageNetPath(face.getImage());
                                            familyItemBean.setRemark(face.getName());
                                            activity.mItems.add(familyItemBean);
                                        }
                                        DBManager.getInstance(activity.getApplicationContext()).deleteFamilyItemBeanAll();
                                        DBManager.getInstance(activity.getApplicationContext()).insertFamilyItemBeanList(activity.mItems);
                                        adapter.notifyDataSetChanged();
                                        mFresh.setEnabled(true);
                                        HashSet<String> set=new HashSet<>();
                                        for (FamilyItemBean mItem : activity.mItems) {
                                            set.add(mItem.getImageLocalPath());
                                        }
                                        File file = new File("/storage/emulated/0/meta");
                                        File[] list = file.listFiles();
                                        for (File s : list) {
                                            if(s.getName().endsWith(".jpg")&&set.add(s.getAbsolutePath())){
                                                Log.e(TAG, "onClick: 多余图片："+s );
                                                FileUtils.deleteImageFile(getContext(),s.getAbsolutePath());
                                            }
                                        }
                                    }
                                });
                            }

                        }else {
                            if( getActivity()!=null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mFresh.setEnabled(true);
                                        ToastUtil.show(getActivity().getApplicationContext(), getString(R.string.synchronization_fail) + data.getCode());
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(final String msg) {
                        if( getActivity()!=null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mFresh.setEnabled(true);
                                    ToastUtil.show(getActivity().getApplicationContext(), msg);
                                }
                            });
                        }
                    }
                });
                break;
            case R.id.family_manage_back:
                if(checkReclick(R.id.family_manage_back,1000))return;
                ((FamilyManageActivity) getActivity()).mHandler.sendEmptyMessage(Constant.FAMILY_DO_BACK);
                break;
        }
    }

    @Override
    public void release() {
        super.release();
        mFamilyManage=null;
        mBack=null;
        this.adapter.release();
        this.adapter=null;
        this.mAdd=null;
        this.mFresh=null;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ((FamilyManageActivity) getActivity()).mPosition = i;
        ((FamilyManageActivity) getActivity()).mHandler.sendEmptyMessage(Constant.FAMILY_DO_UPDATE);
    }
}
