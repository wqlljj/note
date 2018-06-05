package com.cloudminds.register.ui.fragment.employee;


import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cloudminds.register.R;
import com.cloudminds.register.broadcast.InternetBroadcast;
import com.cloudminds.register.callback.OnClickCallback;
import com.cloudminds.register.databinding.FragmentEmployeeImageBinding;
import com.cloudminds.register.repository.db.entity.EmployeeEntity;
import com.cloudminds.register.repository.network.Constant;
import com.cloudminds.register.ui.MainActivity;
import com.cloudminds.register.ui.fragment.BaseFragment;
import com.cloudminds.register.ui.view.ProgressDialogHandler;
import com.cloudminds.register.utils.InjectorUtils;
import com.cloudminds.register.utils.Utils;

import java.io.File;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class EmployeeImageFragment extends BaseFragment {

    private static final String TAG = "EmployeeImageFragment";
    private static final int CAMERA_PHOTO_REQUEST_CODE = 1;
    private File mFile;

    private FragmentEmployeeImageBinding mBinding;
    private ProgressDialogHandler mProgress;
    private EmployeeEntity mEmployee;
    private EmployeeViewModel mModel;

    private final CompositeDisposable mDisposable = new CompositeDisposable();

    private final OnClickCallback mPreviewCallback = () -> {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED) && getActivity() != null) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
            mFile = Utils.getEmployeeImage();
            Uri imageUri = FileProvider.getUriForFile(
                    getActivity(),
                    getActivity().getPackageName() + ".fileprovider",
                    mFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            if(intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivityForResult(intent, CAMERA_PHOTO_REQUEST_CODE);
            } else  {
                Log.w(TAG,"start activity failed");
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_employee_image, container, false);
        init();
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity()).getSupportActionBar().show();
        EmployeeViewModelFactory factory = InjectorUtils.provideEmployeeViewModelFactory(getActivity());
        mModel = ViewModelProviders.of(this, factory).get(EmployeeViewModel.class);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_PHOTO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Bitmap bitmap = Utils.decodeSampledBitmapFromResource(this.mFile.getPath(), 300, 480);
            try {
                Log.e("EmployeeImageFragment", "onActivityResult: 1" + Utils.getFileSizeString(mFile));
                Utils.saveBitmap(bitmap, this.mFile, 20);
                Log.e("EmployeeImageFragment", "onActivityResult: 2" + Utils.getFileSizeString(mFile));
            } catch (Exception e) {
                e.printStackTrace();
            }
            showAvatar();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.next).setTitle(getString(R.string.menu_next));
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.next:
                onNext();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }

    public void setEmployee(EmployeeEntity employee) {
        mEmployee = employee;
    }

    private void init() {
        mBinding.setPreviewClickCallback(mPreviewCallback);
        mBinding.setIsPhotoExist(false);
        mProgress = new ProgressDialogHandler(getActivity(), getString(R.string.Uploading));
        //avatar
        if(mEmployee != null) {
            mFile = mEmployee.getAvatar();
            Log.i(TAG, "init mEmployee = " + mEmployee);
            if (mFile == null && mEmployee.getPhotoPath() != null && !"".equals(mEmployee.getPhotoPath())) {
                showDefaultAvatar(Constant.BASE_URL + mEmployee.getPhotoPath().substring(1));
            } else {
                showAvatar();
            }
        } else {
            Log.w(TAG,"employee avatar file is null");
        }
    }

    private void onNext() {
        Log.d(TAG, "onNext = employee = " + mEmployee);
        if (mBinding.getIsPhotoExist()) {
            mProgress.obtainMessage(ProgressDialogHandler.SHOW_PROGRESS_DIALOG).sendToTarget();
            mModel.setEntity(mEmployee);
            if (!InternetBroadcast.isNetworkAvailable())
            {
                uploadFailure(getString(R.string.network_unavailable));
                return;
            }
            mDisposable.add(mModel.postEmployee()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(responseEntity -> {
                        Log.i(TAG, "onResponse response msg = " + responseEntity.getMsg() + " status = " + responseEntity.getStatus());
                        if ("true".equals(responseEntity.getStatus())) {
                            uploadSuccess();
                        } else {
                            uploadFailure(responseEntity.getMsg());
                        }
                    }, throwable -> uploadFailure(throwable.getMessage()))
            );
        } else {
            Toast.makeText(getActivity(), R.string.employee_request_problem, Toast.LENGTH_SHORT).show();
            Log.w(TAG, "doPostEmployee isn't requested,isRequest is false ");
        }
    }

    /**
     * Upload success
     */
    private void uploadSuccess() {
        mProgress.obtainMessage(ProgressDialogHandler.DISMISS_PROGRESS_DIALOG).sendToTarget();
        Toast.makeText(getActivity(), getString(R.string.entity_request_success), Toast.LENGTH_SHORT).show();
        mFile = null;
        getFragmentManager().popBackStack();
        if (getActivity() != null) {
            ((MainActivity) getActivity()).showSuccessFragment();
        }
    }

    /**
     * Upload failure
     *
     * @param msg Response
     */
    private void uploadFailure(String msg) {
        mProgress.obtainMessage(ProgressDialogHandler.DISMISS_PROGRESS_DIALOG).sendToTarget();
        Toast.makeText(getActivity(), "request failure : " + msg, Toast.LENGTH_SHORT).show();
        Log.i(TAG, "msg = " + msg);
    }

    private void showAvatar() {
        if (mFile != null) {
            Bitmap bitmap = Utils.decodeSampledBitmapFromResource(
                    mFile.getPath(), 300, 480);
            mBinding.ivVisitorPhotoPreview.setImageBitmap(bitmap);
            mBinding.setIsPhotoExist(true);
            mEmployee.setAvatar(mFile);
            Log.d(TAG, "showAvatar Employee set avatar");
        } else {
            Log.w(TAG, "showAvatar temp file is null");
        }
    }

    private void showDefaultAvatar(String url) {
        mBinding.setIsPhotoExist(true);
        Glide.with(this)
                .load(url)
                .into(mBinding.ivVisitorPhotoPreview);
    }

}
