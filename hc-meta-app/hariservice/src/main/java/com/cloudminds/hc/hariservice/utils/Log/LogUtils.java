package com.cloudminds.hc.hariservice.utils.Log;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.cloudminds.hc.hariservice.HariServiceClient;
import com.cloudminds.hc.hariservice.utils.ZipUtil;
import com.getui.logful.LoggerFactory;

import org.apache.log4j.Logger;

import java.io.File;

/**
 * Log统一管理类
 * 
 */
public class LogUtils {
	public static boolean isDebug = true;// 是否需要打印bug，可以在application的onCreate函数里面初始化
	private static final String TAG = "smart";
	private static com.getui.logful.Logger logger = LoggerFactory.logger("HSLogger");
	private static boolean uploadLogEnable = true;
	public static void configure(Context context){
		ConfigureLog4j.configure(context);
		HariServiceClient.initLogger();
	}

	public static void setUploadLogEnable(boolean enable){
		uploadLogEnable = enable;
	}


	// 下面四个是默认tag的函数
	public static void i(String msg) {
		if (isDebug){
			Logger LOGGER = Logger.getLogger(TAG);
			LOGGER.info(msg);
			if (uploadLogEnable){
				logger.info(TAG,msg);
			}
		}
	}

	public static void d(String msg) {
		if (isDebug){
			Logger LOGGER = Logger.getLogger(TAG);
			LOGGER.debug(msg);

			if (uploadLogEnable){
				logger.debug(TAG,msg);
			}
		}
	}

	public static void e(String msg) {
		if (isDebug){
			Logger LOGGER = Logger.getLogger(TAG);
			LOGGER.error(msg);

			if (uploadLogEnable){
				logger.error(TAG,msg);
			}
		}
	}

	public static void v(String msg) {
		if (isDebug){
			Logger LOGGER = Logger.getLogger(TAG);
			LOGGER.info(msg);

			if (uploadLogEnable){
				logger.verbose(TAG,msg);
			}
		}
	}

	// 下面是传入类名打印log
	public static void i(Class<?> _class, String msg) {
		if (isDebug){
			Logger LOGGER = Logger.getLogger(_class.getName());
			LOGGER.info(msg);
		}
	}

	public static void d(Class<?> _class, String msg) {
		if (isDebug){
			Logger LOGGER = Logger.getLogger(_class.getName());
			LOGGER.debug(msg);
		}
	}

	public static void e(Class<?> _class, String msg) {
		if (isDebug){
			Logger LOGGER = Logger.getLogger(_class.getName());
			LOGGER.error(msg);
		}
	}

	public static void v(Class<?> _class, String msg) {
		if (isDebug){
			Logger LOGGER = Logger.getLogger(_class.getName());
			LOGGER.info(msg);
		}
	}

	// 下面是传入自定义tag的函数
	public static void i(String tag, String msg) {
		if (isDebug){
			Logger LOGGER = Logger.getLogger(tag);
			LOGGER.info(msg);

			if (uploadLogEnable){
				logger.info(tag,msg);
			}
		}
	}

	public static void d(String tag, String msg) {
		if (isDebug){
			Logger LOGGER = Logger.getLogger(tag);
			LOGGER.debug(msg);

			if (uploadLogEnable){
				logger.debug(tag,msg);
			}
		}
	}

	public static void e(String tag, String msg) {
		if (isDebug){
			Logger LOGGER = Logger.getLogger(tag);
			LOGGER.error(msg);

			if (uploadLogEnable){
				logger.error(tag,msg);
			}
		}
	}

	public static void v(String tag, String msg) {
		if (isDebug){
			Logger LOGGER = Logger.getLogger(tag);
			LOGGER.info(msg);

			if (uploadLogEnable){
				logger.verbose(tag,msg);
			}
		}
	}

	public static void w(String tag, String msg) {
		if (isDebug){
			Logger LOGGER = Logger.getLogger(tag);
			LOGGER.warn(msg);

			if (uploadLogEnable){
				logger.warn(tag,msg);
			}
		}
	}

	public static void zipLogFile(){
		String sdcardPath = Environment.getExternalStorageDirectory().getPath();
		String logPath = sdcardPath+"/HS/Log";
		String zipPath = sdcardPath+"/HS/log.zip";
		File file = new File(Environment.getExternalStorageDirectory()+"/HS/",
				"log.zip");

		try {
			if (file.exists()) {
				file.delete();
			}
			ZipUtil.zipFolder(logPath,zipPath);
		}catch (Exception e){
			e.printStackTrace();
		}

	}

}
