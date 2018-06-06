package com.cloudminds.meta.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudminds.hc.cloudService.api.HCApiClient;
import com.cloudminds.hc.cloudService.bean.FaceBean;
import com.cloudminds.hc.cloudService.bean.FaceResponse;
import com.cloudminds.hc.cloudService.http.HCBaseHttp;
import com.cloudminds.hc.metalib.utils.ToastUtil;
import com.cloudminds.meta.R;
import com.cloudminds.meta.activity.FamilyManageActivity;
import com.cloudminds.meta.application.MetaApplication;
import com.cloudminds.meta.bean.FamilyItemBean;
import com.cloudminds.meta.constant.Constant;
import com.cloudminds.meta.greendao.DBManager;
import com.cloudminds.meta.util.FileUtils;
import com.cloudminds.meta.util.StringUtils;
import com.cloudminds.meta.util.TTSSpeaker;
import com.cloudminds.meta.view.RemoveFriendsDialog;
import com.cloudminds.meta.view.TakePhotosDialog;
import com.kongqw.OpenCVApi;
import com.yancy.gallerypick.config.GalleryConfig;
import com.yancy.gallerypick.config.GalleryPick;
import com.yancy.gallerypick.inter.IHandlerCallBack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.PhotoPicker;

import static android.R.attr.id;
import static com.cloudminds.meta.constant.Constant.HUB_CONN_IN_CONNECTION;
import static com.cloudminds.meta.constant.Constant.HUB_CONN_ON_CONNECTION;


/**
 * Created by tiger on 17-4-12.
 */

public class FamilyAddOrUpdateFragment extends BaseFamilyFragment implements View.OnClickListener {

    public static final String TAG = "FamilyAddOrUpdate";
    private static final int CODE_GALLERY_REQUEST = 0xa0;
    private static final int CODE_CAMERA_REQUEST = 0xa1;
    private static final int CODE_RESULT_REQUEST = 0xa2;

    private static int output_X = 178;
    private static int output_Y = 220;

    public static String ImagePath = Environment.getExternalStorageDirectory()+"/meta/";
    public File mCurrentFile;
    private int pictureType=0;//0无操作。1正在拍照，2正在选择照片,3,图片剪裁
    private int faceNum=0;

    private TextView mComplete,mCancle,mAdd;
    private EditText mName,mRemark;
    public TakePhotosDialog mDialog;
    private ImageView mImage;
    private FamilyItemBean mBean;
    private Button mRemove;
    private RemoveFriendsDialog mRemoveDialog;

    private GalleryConfig galleryConfig;
    private List<String> path = new ArrayList<>();
    private final int PERMISSIONS_REQUEST_READ_CONTACTS = 8;
    private IHandlerCallBack iHandlerCallBack;

    Uri uri;


    public static FamilyAddOrUpdateFragment newInstance(int type) {
        FamilyAddOrUpdateFragment f = new FamilyAddOrUpdateFragment();
           Bundle args = new Bundle();
           args.putInt("type", type);
           f.setArguments(args);
           return f;
       }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.type = this.getArguments().getInt("type");
        Log.e(TAG, "onCreate: " );
    }

    @Override
    public void onDestroyView() {
        mName.setText("");
        Log.e(TAG, "onDestroyView: "+mName.getText().toString() );
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        Log.e(TAG, "onDetach: " );
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: " );
        super.onDestroy();
        mComplete=null;
        mCancle=null;
        mAdd=null;
        mName=null;
        mRemark=null;
        mDialog=null;
        mImage=null;
        mRemove=null;
        mBean=null;
        mRemoveDialog=null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView: pictureType = "+pictureType );
        mBean = new FamilyItemBean();
        View view = inflater.inflate(R.layout.family_item_add_fragment,container,false);
        mImage = (ImageView) view.findViewById(R.id.family_item_add_image);
        mName = (EditText) view.findViewById(R.id.family_item_add_name);
        mRemark = (EditText) view.findViewById(R.id.family_item_add_remark);
        mAdd = (TextView) view.findViewById(R.id.family_item_add_photo);
        mComplete = (TextView) view.findViewById(R.id.family_item_add_complete);
        mCancle = (TextView) view.findViewById(R.id.family_item_add_cancle);
        mRemove = (Button) view.findViewById(R.id.family_item_remove_friends);
        mComplete.setOnClickListener(this);
        mCancle.setOnClickListener(this);
        mAdd.setOnClickListener(this);
        mImage.setOnClickListener(this);
        initData();

        return view;
    }
    private void setEnabled(boolean flag){
        Log.e(TAG, "setEnabled: "+flag );
        mComplete.setEnabled(flag);
        mCancle.setEnabled(flag);
        mImage.setEnabled(flag);
        mName.setEnabled(flag);
        mRemark.setEnabled(flag);
        mAdd.setEnabled(flag);
        if(((FamilyManageActivity) getActivity()).mType==Constant.FAMILY_TYPE_UPDATE) {
            mRemove.setEnabled(flag);
        }
    }
    private void init() {
        iHandlerCallBack = new IHandlerCallBack() {
            @Override
            public void onStart() {
                Log.i(TAG, "onStart: 开启");
            }

            @Override
            public void onSuccess(List<String> photoList) {
                //FamilyAddOrUpdateFragment.this.getContext().getPackageName()+".provider"
                pictureType=0;
                Log.i(TAG, "onSuccess: 返回数据  \n"+photoList.toString());
                uri = FileProvider.getUriForFile(FamilyAddOrUpdateFragment.this.getContext(),"com.cloudminds.meta.fileprovider", new File(photoList.get(0)));
                cropRawPhoto(uri);
            }

            @Override
            public void onCancel() {
                pictureType=0;
                Log.i(TAG, "onCancel: 取消");
            }

            @Override
            public void onFinish() {
                if(pictureType!=3) {
                    pictureType = 0;
                }
                Log.i(TAG, "onFinish: 结束");
            }

            @Override
            public void onError() {
                pictureType=0;
                Log.i(TAG, "onError: 出错");
            }
        };
        galleryConfig = new GalleryConfig.Builder()
//                .imageLoader(new GlideImageLoader())    // ImageLoader 加载框架（必填）
                .useGlideImageLoader()
                .iHandlerCallBack(iHandlerCallBack)     // 监听接口（必填）
                .provider("com.cloudminds.meta.fileprovider")   // provider(必填)
                .pathList(path)                         // 记录已选的图片
                .multiSelect(false)                      // 是否多选   默认：false
                .multiSelect(false, 9)                   // 配置是否多选的同时 配置多选数量   默认：false ， 9
                .maxSize(9)                             // 配置多选时 的多选数量。    默认：9
                .crop(false)                             // 快捷开启裁剪功能，仅当单选 或直接开启相机时有效
                .crop(false, 1, 1, 500, 500)             // 配置裁剪功能的参数，   默认裁剪比例 1:1
                .isShowCamera(true)                     // 是否现实相机按钮  默认：false
                .filePath("/meta")          // 图片存放路径
                .build();
    }
    // 授权管理
    private void initPermissions() {
        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "需要授权 ");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Log.i(TAG, "拒绝过了");
                Toast.makeText(this.getContext(), "请在 设置-应用管理 中开启此应用的储存授权。", Toast.LENGTH_SHORT).show();
            } else {
                Log.i(TAG, "进行授权");
                ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        } else {
            Log.i(TAG, "不需要授权 ");
            GalleryPick.getInstance().setGalleryConfig(galleryConfig).open(this.getActivity());
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "同意授权");
                GalleryPick.getInstance().setGalleryConfig(galleryConfig).open(this.getActivity());
            } else {
                Log.i(TAG, "拒绝授权");
            }
        }
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onStop: " );
        super.onStop();
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume: pictureType = "+pictureType );
        super.onResume();
        switch (pictureType){
            case 1:
                if(MetaApplication.state==HUB_CONN_ON_CONNECTION||MetaApplication.state==HUB_CONN_IN_CONNECTION){
                    TTSSpeaker.speak(getString(R.string.camera_unable_calling),TTSSpeaker.HIGH);
                    ToastUtil.show(getActivity().getApplicationContext(), R.string.camera_unable_calling);
                    mDialog.dismiss();
                    return;
                }
                pictureType=1;
                galleryConfig.getBuilder().isOpenCamera(true).build();
//                GalleryPick.getInstance().setGalleryConfig(galleryConfig).openCamera(this.getActivity());
                initPermissions();
                break;
            case 2:
                galleryConfig.getBuilder().isOpenCamera(false).build();
                initPermissions();
                break;
            case 3:
                if(uri!=null) {
                    cropRawPhoto(uri);
                }
                break;
        }
    }

    private void initData(){
                FamilyManageActivity activity = (FamilyManageActivity) getActivity();
        Log.e(TAG, "initData: "+ activity.mType );
        if( activity.mType == Constant.FAMILY_TYPE_UPDATE){
                    mAdd.setText(R.string.update_pictur);
                    mRemove.setVisibility(View.VISIBLE);
                    mRemove.setOnClickListener(FamilyAddOrUpdateFragment.this);
                    mBean.copy(activity.mItems.get(activity.mPosition));
                    Log.e(TAG, "initData: UPDATE "+activity.mPosition+"  \n"+mBean.toString() );
            Bitmap bitmap = scaleImage(mBean.getImageLocalPath());
            faceNum = OpenCVApi.detectFaceNum(bitmap);
            mImage.setImageBitmap(bitmap);
                    mName.setText(mBean.getName());
//            mName.setText(this.toString());
            Log.e(TAG, "initData: "+ mName.getText().toString());
                    mRemark.setText(mBean.getRemark());
                }else{
                    Log.e(TAG, "initData: ADD" );
                    mName.setText("");
                    mRemark.setText("");
                    mAdd.setText(R.string.family_item_add_photo);
                }
        mName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if(mName.getCurrentTextColor()==getResources().getColor(R.color.red)){
                    mName.setTextColor(getResources().getColor(R.color.black));
                }
            }
        });
        init();
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

    @Override
    public void onClick(View view) {
        FamilyManageActivity activity = (FamilyManageActivity) getActivity();
        switch (view.getId()){
            case R.id.family_item_add_cancle://返回
                if (checkReclick(view.getId(),1000)) return;
                Log.e(TAG, "onClick:setEnabled(false) 1" );
                setEnabled(false);
                activity.mHandler.sendEmptyMessage(Constant.FAMILY_DO_ADD_BACK);
                break;
            case R.id.family_item_add_complete://完成
                Log.d(TAG,"add success !");
                if (checkReclick(view.getId(),1500)) return;
                Log.e(TAG, "onClick:setEnabled(false) 2" );
                setEnabled(false);
                if(faceNum!=1){
                    ToastUtil.show(activity,R.string.photo_error_1);
                    Log.e(TAG, "onClick:setEnabled(true) 3" );
                    setEnabled(true);
                    return;
                }
                String name = mName.getText().toString();
                int qualified = StringUtils.isNameQualified(name);
                if(qualified !=0){
                    int id=-1;
                    mName.setTextColor(getResources().getColor(R.color.red));
                    switch (qualified){
                        case 1:
                            id=R.string.name_error_1;
                            break;
                        case 2:
                            id=R.string.name_error_2;
                            break;
                        case 3:
                            id=R.string.name_error_3;
                            break;
                        case 4:
                            id=R.string.name_error_4;
                            break;
                        case 5:
                            id=R.string.name_error_5;
                            break;
                        case 6:
                            id=R.string.name_error_6;
                            break;
                        default:
                            id=R.string.name_wrong;
                            break;
                    }
                    ToastUtil.show(activity,id);
                    Log.e(TAG, "onClick:setEnabled(true) 4" );
                    setEnabled(true);
                    return;
                }
                showWaitDialog();
                mBean.setRemark(mRemark.getText().toString());
                mBean.setName(name);
                    addFace();
                break;
            case R.id.family_item_add_image://图片添加或修改
            case R.id.family_item_add_photo:
                if (checkReclick(view.getId(),1000)) return;
                showDialog();
                break;
            case R.id.bt_choose_picture://选择图片
                if (checkReclick(view.getId(),1000)) return;
                Log.d(TAG,"choose_picture");
                pictureType=2;
                galleryConfig.getBuilder().isShowCamera(false).isOpenCamera(false).build();
                initPermissions();
//                choosePicture();
                mDialog.dismiss();
                break;
            case R.id.bt_take_picture://拍照
                if (checkReclick(view.getId(),1000)) return;
                Log.d(TAG,"bt_take_picture");
                if(MetaApplication.state==HUB_CONN_ON_CONNECTION||MetaApplication.state==HUB_CONN_IN_CONNECTION){
                    TTSSpeaker.speak(getString(R.string.camera_unable_calling),TTSSpeaker.HIGH);
                    ToastUtil.show(activity.getApplicationContext(), R.string.camera_unable_calling);
                    mDialog.dismiss();
                    return;
                }
                pictureType=1;
                galleryConfig.getBuilder().isOpenCamera(true).build();
//                GalleryPick.getInstance().setGalleryConfig(galleryConfig).openCamera(this.getActivity());
                initPermissions();
//                takePicture();
                mDialog.dismiss();
                break;
            case R.id.bt_cancel://取消图片操作选择弹窗
                if (checkReclick(view.getId(),1000)) return;
                Log.d(TAG,"bt_cancel");
                mDialog.dismiss();
                break;
            case R.id.family_item_remove_friends://删除亲友
                if (checkReclick(view.getId(),1000)) return;
                Log.e(TAG, "onClick:setEnabled(false) 5" );
                setEnabled(false);
                showRemoveDialog();
                break;
            case R.id.equipment_dialog_cancle://删除亲友弹窗取消
                if (checkReclick(view.getId(),1000)) return;
                Log.e(TAG, "onClick:setEnabled(true) 6" );
                setEnabled(true);
                mRemoveDialog.dismiss();
                break;
            case R.id.equipment_dialog_ok://删除亲友弹窗删除
                if (checkReclick(view.getId(),1000)) return;
                showWaitDialog();
                deleteFace(R.id.equipment_dialog_ok,activity.mItems.get(activity.mPosition).getFace_id());
                mRemoveDialog.dismiss();
                break;
        }
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
    int update_type=0;//0,更新成功，-1更新失败
    private void deleteFace(@IdRes final int id,String faceid) {
        Log.e(TAG, "deleteFace: "+id );
        final FamilyManageActivity activity = (FamilyManageActivity) getActivity();
        HCApiClient.deleteFace(faceid, new HCBaseHttp.CallBack<FaceResponse>() {
            @Override
            public void onResponse(final FaceResponse data) {
                if(data.getCode()==0){
//                    mBean.setFace_id(data.getFace_id());
                    removeFriends();
                            if(id==R.id.family_item_add_complete) {
                                saveLocal(activity);
                            }else{
                                Log.e(TAG, "onResponse:setEnabled(true) 1" );
                                setEnabled(true);
                                if(update_type!=-1){
                                    ToastUtil.show(activity.getApplicationContext(), R.string.delete_success);
                                    Log.e(TAG, "onResponse:setEnabled(false) 2" );
                                    setEnabled(false);
                                    activity.mHandler.sendEmptyMessage(Constant.FAMILY_DO_ADD_BACK);
                                }
                            }

                }else {
                    Log.e(TAG, "onResponse:setEnabled(true) 3" );
                    setEnabled(true);
                    if(id==R.id.family_item_add_complete) {
                        update_type=-1;
                        deleteFace(R.id.equipment_dialog_ok, mBean.getFace_id());
                        ToastUtil.show(activity.getApplicationContext(), getString(R.string.update_error) + data.getCode());
                    }else if(update_type!=-1){
                        ToastUtil.show(activity.getApplicationContext(), getString(R.string.delete_fail) + data.getCode());
                    }
                }
                if(btId==R.id.equipment_dialog_ok)
                dialog.dismiss();
            }

            @Override
            public void onFailure(final String msg) {
                Log.e(TAG, "onFailure:setEnabled(true) 1" );
                setEnabled(true);
                ToastUtil.show(activity.getApplicationContext(), msg);
                if(id==R.id.family_item_add_complete) {
                    update_type=-1;
                    deleteFace(R.id.equipment_dialog_ok, mBean.getFace_id());
                }
                if(btId==R.id.equipment_dialog_ok) {
                    dialog.dismiss();
                }
            }
        });
    }

    private void addFace() {
        final FamilyManageActivity activity = (FamilyManageActivity) getActivity();
        if(mBean==null){
            ToastUtil.show(activity.getApplicationContext(), R.string.data_error);
            Log.e(TAG, "addFace:setEnabled(true) 1" );
            setEnabled(true);
            dialog.dismiss();
            return;
        }
        if(TextUtils.isEmpty(mBean.getName())){
            ToastUtil.show(activity.getApplicationContext(), R.string.name_null);
            Log.e(TAG, "addFace:setEnabled(true) 2" );
            setEnabled(true);
            dialog.dismiss();
            return;
        }
        if(TextUtils.isEmpty(mBean.getImageLocalPath())){
            ToastUtil.show(activity.getApplicationContext(), R.string.image_null);
            Log.e(TAG, "addFace:setEnabled(true) 3" );
            setEnabled(true);
            dialog.dismiss();
            return;
        }
        HCApiClient.addFace(mBean.getName(), mBean.getImageLocalPath(), new HCBaseHttp.CallBack<FaceResponse>() {
            @Override
            public void onResponse(final FaceResponse data) {
                String error="";
                switch (data.getCode()){
                    case 0:
                        Log.e(TAG, "onResponse: "+data.toString() );
                            mBean.setFace_id(data.getFace_id());
                            if (activity.mType == Constant.FAMILY_TYPE_UPDATE) {
                                deleteFace(R.id.family_item_add_complete, activity.mItems.get(activity.mPosition).getFace_id());
                            } else {
                                saveLocal(activity);
                            }
                        break;
                    case 98:
                        error=getString(R.string.no_face);
                        break;
                    default:
                        error=""+data.getCode();
                }
                if(!TextUtils.isEmpty(error)) {
                    Log.e(TAG, "addFace:setEnabled(true) 4" );
                    setEnabled(true);
                    ToastUtil.show(activity.getApplicationContext(), getString(R.string.upload_fail) + error);
                }
                if(btId==R.id.family_item_add_complete)clickTime=0l;
                dialog.dismiss();
            }

            @Override
            public void onFailure(final String msg) {
                if(btId==R.id.family_item_add_complete)clickTime=0l;
                ToastUtil.show(activity.getApplicationContext(), msg);
                Log.e(TAG, "addFace:setEnabled(true) 5" );
                setEnabled(true);
                dialog.dismiss();
            }
        });
    }

    private void saveLocal(final FamilyManageActivity activity) {
        HCApiClient.getFaceList(new HCBaseHttp.CallBack<FaceResponse>() {
            @Override
            public void onResponse(final FaceResponse data) {
                if(data.getCode()==0) {
//                            DBManager.getInstance(getContext()).insertFamilyItemBean(mBean);
                    if(getActivity()!=null) {
                        getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "同步成功 :" + data.toString());
                            List<FaceBean> faces = data.getFaces();
                            for (FaceBean face : faces) {
                                if (face.getFace_id().equals(mBean.getFace_id())) {
                                    mBean.setImageNetPath(face.getImage());
                                    String imageLocalPath = mBean.getImageLocalPath();
                                    FileUtils.deleteImageFile(getContext(),imageLocalPath);
                                    mBean.setImageLocalPath(imageLocalPath.replace(imageLocalPath.substring(imageLocalPath.lastIndexOf("/")),
                                            face.getImage().substring(face.getImage().lastIndexOf("/"))));
                                    Log.e(TAG, "saveLocal: " + mBean.toString());
                                    activity.mItems.add(mBean);
                                    DBManager.getInstance(getContext()).insertFamilyItemBean(mBean);
                                    ToastUtil.show(activity.getApplicationContext(), R.string.upload_success);
//                                    setEnabled(true);
                                    activity.mHandler.sendEmptyMessage(Constant.FAMILY_DO_ADD_BACK);
                                    return;
                                }
                            }
                            if (activity.mType == Constant.FAMILY_TYPE_UPDATE) {
                                update_type = -1;
                            }
                            deleteFace(R.id.equipment_dialog_ok, mBean.getFace_id());
                        }
                    });
                }

                }else {
                    if(getActivity()!=null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (activity.mType == Constant.FAMILY_TYPE_UPDATE) {
                                    update_type = -1;
                                }
                                deleteFace(R.id.equipment_dialog_ok, mBean.getFace_id());
                                ToastUtil.show(getActivity().getApplicationContext(), getString(R.string.synchronization_fail) + data.getCode());
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(final String msg) {
                if(getActivity()!=null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (activity.mType == Constant.FAMILY_TYPE_UPDATE) {
                                update_type = -1;
                            }
                            deleteFace(R.id.equipment_dialog_ok, mBean.getFace_id());
                            ToastUtil.show(getActivity().getApplicationContext(), msg);
                        }
                    });
                }
            }
        });
    }

    Dialog dialog;
    private void showWaitDialog(){
        dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.waitdialog);
        ImageView icon = (ImageView) dialog.findViewById(R.id.icon);
        Animation rotateAnimation  = new RotateAnimation(0, 359, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setDuration(5000);
        rotateAnimation.setRepeatCount(-1);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        icon.startAnimation(rotateAnimation);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void showRemoveDialog() {
        Activity activity = getActivity();
        RemoveFriendsDialog.Builder builder = new RemoveFriendsDialog.Builder();
        mRemoveDialog = builder.setTitle(R.string.remove_family)
                .setMessage(String.format(activity.getResources().getString(R.string.
                        remove_family_sure),mBean.getName()))
                .setCancle(R.string.remove_family_cancle)
                .setCancleListener(this)
                .setOk(R.string.remove_family_ok)
                .setOkListener(this)
                .builder();
        mRemoveDialog.show(activity);
    }

    private void removeFriends(){
        Log.e(TAG, "removeFriends: " );
        FamilyManageActivity activity = (FamilyManageActivity) getActivity();
            if(update_type==0) {
                FileUtils.deleteImageFile(getContext(),activity.mItems.get(activity.mPosition).getImageLocalPath());
                DBManager.getInstance(getContext()).deleteFamilyItemBean(activity.mItems.get(activity.mPosition));
                if(activity!=null&&activity.mItems!=null) {
                    activity.mItems.remove(activity.mPosition);
                }
            }else{
                update_type=0;
            }
    }

    private void showDialog(){
        Activity activity = getActivity();
        mDialog = new TakePhotosDialog(activity, R.style.TakePhoto_Dialog);//设置dialog的样式
        Window window = mDialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        mDialog.show();
        WindowManager windowManager =activity.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams params = mDialog.getWindow().getAttributes();
        params.width = (int) ((display.getWidth())*0.9);
        window.setAttributes(params);
        mDialog.findViewById(R.id.bt_choose_picture).setOnClickListener(this);
        mDialog.findViewById(R.id.bt_take_picture).setOnClickListener(this);
        mDialog.findViewById(R.id.bt_cancel).setOnClickListener(this);
    }

    private void takePicture(){
        Activity activity = getActivity();
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(activity.getPackageManager()) != null) {
            File path = new File(ImagePath);
            if(!path.exists()){
                path.mkdir();
            }
            mCurrentFile = new File(ImagePath, System.currentTimeMillis()+".jpg");
            Uri uri = FileProvider.getUriForFile(activity,activity.getPackageName()+".provider", mCurrentFile);
            Log.d(TAG,"filePath = "+uri.toString());
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            List<ResolveInfo> resInfoList = activity.getPackageManager().queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                activity.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            activity.startActivityForResult(cameraIntent, CODE_CAMERA_REQUEST);
        }
    }

    private void choosePicture(){
        PhotoPicker.builder()
                .setPhotoCount(1)
                .setShowCamera(false)
                .setShowGif(true)
                .setPreviewEnabled(false)
                .start(getActivity(), PhotoPicker.REQUEST_CODE);
    }

    public void setImageToView(Intent intent) {
        pictureType=0;
        uri=null;
        Bundle extras = intent.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");

            if(photo!=null){
                faceNum = OpenCVApi.detectFaceNum(photo);
                Log.e(TAG, "setImageToView: faceNum = "+faceNum );
                mImage.setImageBitmap(photo);
                if(faceNum==1) {
                    String path = ImagePath + System.currentTimeMillis() + ".jpg";
                    File file = new File(path);
                    if (!file.exists()) {
                        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        photo.compress(Bitmap.CompressFormat.JPEG, 85, new FileOutputStream(path));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    mBean.setImageLocalPath(path);
                }
            }
        }
    }

    public void cropRawPhoto(Uri uri) {
        pictureType=3;
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", output_X);
        intent.putExtra("outputY", output_Y);
        intent.putExtra("return-data", true);
        getActivity().startActivityForResult(intent, CODE_RESULT_REQUEST);
    }

}
