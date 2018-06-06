package com.cloudminds.hc.metalib.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Locale;

/**
 * Created by willzhang on 19/06/17
 */

public final class MD5 {

    private MD5() {
    }

    public static boolean check(File file, String md5) throws FileNotFoundException, IllegalArgumentException {
        String fileMD5 = get(file);
        return md5.equalsIgnoreCase(fileMD5);
    }

    private static String get(File file) throws FileNotFoundException, IllegalArgumentException {
        if (null == file || !file.exists()) {
            throw new FileNotFoundException("");
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("Not a file");
        }
        MessageDigest digest;
        FileInputStream in;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        BigInteger bigint = new BigInteger(1, digest.digest());
        return String.format(Locale.ENGLISH, "%032x", bigint); // 不足32位自动补0

    }
}
