package com.cloudminds.hc.metalib.features.installation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.widget.TextView;

import com.cloudminds.hc.metalib.R;
import com.cloudminds.hc.metalib.features.pojo.ActualVersionInfo;
import com.cloudminds.hc.metalib.features.pojo.Packages;
import com.cloudminds.hc.metalib.utils.NetworkUtil;
import com.cloudminds.hc.metalib.utils.ToastUtil;


/**
 * Created by willzhang on 23/06/17
 */

public class VersionInfoDialog {

    public static final long SIZE_MOBILE_DATA_LIMIT = 1024 * 1024 * 100; // 100M

    private final Context mContext;
    private final ActualVersionInfo versionInfo;
    private AlertDialog versionInfoDialog;
    private AlertDialog packageSizeDialog;

    public VersionInfoDialog(final Context mContext, final ActualVersionInfo versionInfo, final DialogInterface.OnClickListener download) {
        this.mContext = mContext;
        this.versionInfo = versionInfo;
        AlertDialog.Builder downloadBuilder = new AlertDialog.Builder(mContext);
        downloadBuilder.setCancelable(true);
        downloadBuilder.setView(R.layout.version_info_dialog);
        downloadBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        downloadBuilder.setPositiveButton(R.string.button_download_now, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Packages packages = versionInfo.getPackages()[0];
                long packageSize = 0;
                try {
                    packageSize = Long.valueOf(packages.getSize());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                int netType = NetworkUtil.getConnectionType(mContext);
                if (-1 == netType) {
                    ToastUtil.show(mContext, R.string.toast_text_no_available_connection);
                    dialog.dismiss();
                    return;
                }
                // 非wifi状态并且package size大于100M，询问土豪是否依然下载
                if (netType != ConnectivityManager.TYPE_WIFI && packageSize > SIZE_MOBILE_DATA_LIMIT) {
                    AlertDialog.Builder warningBuilder = new AlertDialog.Builder(mContext);
                    warningBuilder.setCancelable(true);
                    warningBuilder.setTitle(R.string.dialog_title_warning);
                    warningBuilder.setMessage(mContext.getString(R.string.dialog_msg_mobile_data_warning));
                    warningBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    warningBuilder.setPositiveButton(R.string.dialog_button_force_download, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 用户是土豪，选择继续下载
                            if (null != download) {
                                download.onClick(dialog, which);
                            }
                        }
                    });
                    packageSizeDialog = warningBuilder.create();
                    packageSizeDialog.show();
                } else { // 目前处于WIFI状态下，或者安装包小于100M，直接下载，不提醒用户
                    if (null != download) {
                        download.onClick(dialog, which);
                    }
                }
            }
        });
        versionInfoDialog = downloadBuilder.create();
    }

    private String toReadableSize(Context mContext, String packageSize) {
        long size = Long.valueOf(packageSize);
        return Formatter.formatFileSize(mContext, size);
    }

    public void show() {
        versionInfoDialog.show();
        TextView versionId = (TextView) versionInfoDialog.findViewById(R.id.versionId);
        TextView packageSize = (TextView) versionInfoDialog.findViewById(R.id.packageSize);
        TextView buildTime = (TextView) versionInfoDialog.findViewById(R.id.buildTime);

        versionId.setText(versionInfo.getSimplename());
        packageSize.setText(toReadableSize(mContext, versionInfo.getPackages()[0].getSize()));
        String description = versionInfo.getDescription();
        description = TextUtils.isEmpty(description) ? mContext.getString(R.string.text_empty_description) : description;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            buildTime.setText(Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY));
        } else {
            buildTime.setText(Html.fromHtml(description));
        }
    }
}
