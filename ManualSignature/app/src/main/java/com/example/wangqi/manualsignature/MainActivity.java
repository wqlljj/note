package com.example.wangqi.manualsignature;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.gcacace.signaturepad.views.SignaturePad;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SignaturePad mSignaturePad;
    private ImageView showImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSignaturePad = (SignaturePad) findViewById(R.id.signature_pad);
        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {

            @Override
            public void onStartSigning() {
            }

            @Override
            public void onSigned() {
//                showImage.setImageBitmap(mSignaturePad.getSignatureBitmap());
            }

            @Override
            public void onClear() {
                //Event triggered when the pad is cleared
                Toast.makeText(MainActivity.this, "onClear", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.save).setOnClickListener(this);
        findViewById(R.id.clear).setOnClickListener(this);
        showImage = findViewById(R.id.show);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear:
                mSignaturePad.clear();
                break;
            case R.id.save:
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) { // 判断是否可以对SDcard进行操作
                    // 获取SDCard指定目录下
                    String sdCardDir = Environment.getExternalStorageDirectory() + "/manualsignature/";
                    File dirFile = new File(sdCardDir);  //目录转化成文件夹
                    if (!dirFile.exists()) {              //如果不存在，那就建立这个文件夹
                        dirFile.mkdirs();
                    }                          //文件夹有啦，就可以保存图片啦
                    File file = new File(sdCardDir, System.currentTimeMillis() + ".jpg");// 在SDcard的目录下创建图片文,以当前时间为其命名
                    FileOutputStream out = null;
                    try {
                        out = new FileOutputStream(file);
                        mSignaturePad.getSignatureBitmap().compress(Bitmap.CompressFormat.JPEG, 90, out);
                        System.out.println("_________保存到____sd______指定目录文件夹下____________________");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (out != null) {
                        try {
                            out.flush();
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(this, "保存已经至" + Environment.getExternalStorageDirectory() + "/manualsignature/" + "目录文件夹下", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainActivityPermissionsDispatcher.needpermissionWithPermissionCheck(this);
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void needpermission() {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showRationale(final PermissionRequest request) {
        request.proceed();
    }
}
