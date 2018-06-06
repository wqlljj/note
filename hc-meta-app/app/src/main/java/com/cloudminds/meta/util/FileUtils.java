package com.cloudminds.meta.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.cloudminds.meta.constant.Constant;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by tiger on 17-4-17.
 */

public class FileUtils {

    public static final String TAG = "Meta:FileUtils";

    public static void makeDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static void copyFromAssetsToSdcard(Context context, String source, String dest) {
        File file = new File(dest);
        Log.d(TAG, "is exists = " + file.exists() + "   " + file.getAbsolutePath());
        if (!file.exists()) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = context.getResources().getAssets().open(source);
                String path = dest;
                fos = new FileOutputStream(path);
                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getSDPath() {
        String sdcardPath = Environment.getExternalStorageDirectory().toString();
        return sdcardPath + "/" + Constant.SAMPLE_DIR_NAME;
    }

    private static void Unzip(String zipFile, String targetDir) {
        int BUFFER = 4096; //这里缓冲区我们使用4KB，
        String strEntry; //保存每个zip的条目名称

        try {
            BufferedOutputStream dest = null; //缓冲输出流
            FileInputStream fis = new FileInputStream(zipFile);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry; //每个zip条目的实例

            while ((entry = zis.getNextEntry()) != null) {

                try {
                    Log.i("Unzip: ", "=" + entry);
                    int count;
                    byte data[] = new byte[BUFFER];
                    strEntry = entry.getName();

                    File entryFile = new File(targetDir + strEntry);
                    File entryDir = new File(entryFile.getParent());
                    if (!entryDir.exists()) {
                        entryDir.mkdirs();
                    }

                    FileOutputStream fos = new FileOutputStream(entryFile);
                    dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = zis.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            zis.close();
        } catch (Exception cwj) {
            cwj.printStackTrace();
        }
    }

    /**
     * 10.     * 删除文件，可以是文件或文件夹
     * 11.     *
     * 12.     * @param fileName
     * 13.     *            要删除的文件名
     * 14.     * @return 删除成功返回true，否则返回false
     * 15.
     */
    public static boolean delete(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("删除文件失败:" + fileName + "不存在！");
            return false;
        } else {
            if (file.isFile())
                return deleteFile(fileName);
            else
                return deleteDirectory(fileName);
        }
    }

    public static boolean saveBitmap(Context context, Bitmap mBitmap, String path) {
        Log.e(TAG, "saveBitmap: path = " + path);
        File filePic;
        try {
            filePic = new File(path);
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static void deleteImageFile(Context context, String path) {
        Log.e(TAG, "deleteImageFile: " + path);
//            ContentResolver resolver = context.getContentResolver();
//                Cursor cursor = MediaStore.Images.Media.query(resolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Images.Media._ID }, MediaStore.Images.Media.DATA + "=?",
//                  new String[] { path }, null);
        boolean result = false;
//                if (cursor.moveToFirst()) {
//                    Log.e(TAG, "deleteImageFile: 1" );
//                   long id = cursor.getLong(0);
//                   Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//                   Uri uri = ContentUris.withAppendedId(contentUri, id);
//                    int count = context.getContentResolver().delete(uri, null, null);
//                    result = count == 1;
//                } else {
        Log.e(TAG, "deleteImageFile: 2");
        File file = new File(path);
        if (file.exists()) {
            result = file.delete();
        } else {
            result = true;
        }
        Log.e(TAG, "deleteImageFile: file.exists = " + file.exists());
//                }

        if (result) {
            Log.e(TAG, "deleteImageFile: 删除成功");
        } else {
            Log.e(TAG, "deleteImageFile: 删除失败");
        }

    }

    /**
     * 30.     * 删除单个文件
     * 31.     *
     * 32.     * @param fileName
     * 33.     *            要删除的文件的文件名
     * 34.     * @return 单个文件删除成功返回true，否则返回false
     * 35.
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                System.out.println("删除单个文件" + fileName + "成功！");
                return true;
            } else {
                System.out.println("删除单个文件" + fileName + "失败！");
                return false;
            }
        } else {
            System.out.println("删除单个文件失败：" + fileName + "不存在！");
            return false;
        }
    }
    public static void insertImageToGallery(Context context,String path){
        Log.e(TAG, "insertImageToGallery: "+path );
    // 其次把文件插入到系统图库
        try{
        MediaStore.Images.Media.insertImage(context.getContentResolver(),
                path, new File(path).getName(), null);
    } catch(FileNotFoundException e){
        e.printStackTrace();
    }
    // 最后通知图库更新
//        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(path))));
}

    /**
 54.     * 删除目录及目录下的文件
 55.     *
 56.     * @param dir
 57.     *            要删除的目录的文件路径
 58.     * @return 目录删除成功返回true，否则返回false
 59.     */
        public static boolean deleteDirectory(String dir) {
            // 如果dir不以文件分隔符结尾，自动添加文件分隔符
            if (!dir.endsWith(File.separator))
                dir = dir + File.separator;
            File dirFile = new File(dir);
            // 如果dir对应的文件不存在，或者不是一个目录，则退出
            if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
                System.out.println("删除目录失败：" + dir + "不存在！");
                return false;
            }
            boolean flag = true;
            // 删除文件夹中的所有文件包括子目录
            File[] files = dirFile.listFiles();
            for (int i = 0; i < files.length; i++) {
                // 删除子文件
                if (files[i].isFile()) {
                    flag = deleteFile(files[i].getAbsolutePath());
                    if (!flag)
                        break;
                }
                // 删除子目录
                else if (files[i].isDirectory()) {
                    flag = deleteDirectory(files[i]
                            .getAbsolutePath());
                    if (!flag)
                        break;
                }
            }
            if (!flag) {
                System.out.println("删除目录失败！");
                return false;
            }
            // 删除当前目录
            if (dirFile.delete()) {
                System.out.println("删除目录" + dir + "成功！");
                return true;
            } else {
                return false;
            }
        }

    }
