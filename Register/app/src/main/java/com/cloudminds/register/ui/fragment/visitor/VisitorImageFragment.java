package com.cloudminds.register.ui.fragment.visitor;


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

import com.cloudminds.register.R;
import com.cloudminds.register.callback.OnClickCallback;
import com.cloudminds.register.databinding.FragmentVisitorImageBinding;
import com.cloudminds.register.repository.network.bean.Visitor;
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
public class VisitorImageFragment extends BaseFragment {

    private static final int CAMERA_PHOTO_REQUEST_CODE = 1;
    private static final String TAG = "VisitorImageFragment";

    private FragmentVisitorImageBinding mBinding;
    private Visitor mVisitor;
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private VisitorViewModel mModel;
    private File mFile;
    private ProgressDialogHandler mProgress;
    private Menu mMenu;

    private final OnClickCallback mPreviewCallback = () -> {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED) && getActivity() != null) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
            mFile = Utils.getVisitorImage();
            Uri imageUri = FileProvider.getUriForFile(
                    getActivity(),
                    getActivity().getPackageName() + ".fileprovider",
                    mFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivityForResult(intent, CAMERA_PHOTO_REQUEST_CODE);
            } else {
                Log.w(TAG, "start activity failed");
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_visitor_image, container, false);
        init();
        return mBinding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mMenu = menu;
        mMenu.findItem(R.id.next).setTitle(getString(R.string.menu_skip));
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity) getActivity()).getSupportActionBar().show();
        VisitorViewModelFactory factory = InjectorUtils.provideVisitorViewModelFactory(getActivity());
        mModel = ViewModelProviders.of(this, factory).get(VisitorViewModel.class);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_PHOTO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (mFile != null) {
                Bitmap bitmap = Utils.decodeSampledBitmapFromResource(
                        mFile.getPath(), 300, 480);
                mBinding.ivVisitorPhotoPreview.setImageBitmap(bitmap);
                mBinding.setIsPhotoExist(true);
                mVisitor.setAvatar(mFile);
                mMenu.findItem(R.id.next).setTitle(getString(R.string.menu_next));
                Log.d(TAG, "Employee set avatar");
            } else {
                Log.w(TAG, "temp file is null");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }

    public void setVisitor(Visitor visitor) {
        this.mVisitor = visitor;
    }

    private void init() {
        mBinding.setPreviewClickCallback(mPreviewCallback);
        mProgress = new ProgressDialogHandler(getActivity(), getString(R.string.Uploading));
        mBinding.setIsPhotoExist(false);

        if (mVisitor != null && mVisitor.getAvatar() != null) {
            mFile = mVisitor.getAvatar();
            Bitmap bitmap = Utils.decodeSampledBitmapFromResource(
                    mVisitor.getAvatar().getPath(), 200, 200);
            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
            mBinding.ivVisitorPhotoPreview.setImageDrawable(drawable);
            mBinding.setIsPhotoExist(true);
            mVisitor.setAvatar(mFile);
            Log.d(TAG, "Employee set avatar");
        } else {
            Log.w(TAG, "temp file is null");
        }

    }

    private void onNext() {
        Log.i(TAG, "mVisitor = " + mVisitor);
        mProgress.obtainMessage(ProgressDialogHandler.SHOW_PROGRESS_DIALOG).sendToTarget();
        mModel.setEntity(mVisitor);
        mDisposable.clear();
        mDisposable.add(mModel.postVisitor()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseEntity -> {
                    Log.i(Utils.TAG, "VisitorRepository onResponse response msg = " + responseEntity.getMsg() + " status = " + responseEntity.getStatus());
                    if ("true".equals(responseEntity.getStatus())) {
                        uploadSuccess();
                    } else {
                        uploadFailure(responseEntity.getMsg());
                    }
                }, throwable -> {
                    Log.d(Utils.TAG, "onFailure response = " + throwable.getMessage());
                    uploadFailure(throwable.getMessage());
                })
        );
    }

    /**
     * Upload success
     */
    private void uploadSuccess() {
        mProgress.obtainMessage(ProgressDialogHandler.DISMISS_PROGRESS_DIALOG).sendToTarget();
        Toast.makeText(getActivity(), getString(R.string.entity_request_success), Toast.LENGTH_SHORT).show();
        mFile = null;
        getFragmentManager().popBackStack();
        ((MainActivity) getActivity()).showSuccessFragment();
    }

    /**
     * Upload failure
     *
     * @param msg Response
     */
    private void uploadFailure(String msg) {
        mProgress.obtainMessage(ProgressDialogHandler.DISMISS_PROGRESS_DIALOG).sendToTarget();
        Toast.makeText(getActivity(), "request failure : " + msg, Toast.LENGTH_SHORT).show();
    }

}
