package com.cloudminds.meta.util;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
	
	
	/**
	 * 将String转成Date
	 * @param date
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public  static Date stringToDate(String date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return sdf.parse(date);
		} catch (ParseException e) {
		}
		return null;
	}
	
	/**
	 * 将String转成hour
	 * @param date
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public  static Date stringToHour(String date){
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		try {
			return  formatter.parse(date);
		} catch (ParseException e) {
			Log.i("stringToHour","将String转成hour  失败");
		}
		return null;
	}
	
//	/**
//	 * 将Hour转成String
//	 * @param date
//	 * @return
//	 */
//	@SuppressLint("SimpleDateFormat")
//	public  static String HourToString(Date date){
//		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
//		return sdf.format(date);
//	}
	
	/**
	 * 将Date转成String
	 * @param date
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public  static String dateToString(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
	}
	@SuppressLint("SimpleDateFormat")
	public  static String getTimeString(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(new Date());
	}

	public  static String dateToAmPm(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		String str = sdf.format(date);
		String[] arr = str.split(":");
		if(Integer.valueOf(arr[0])>=12){
			return str+"PM";
		}else{
			return str+"AM";
		}
	}
	
	public  static String dateToString(Long date){
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
		return sdf.format(date);
	}
	
	
	public  static String dateToFormatString(Long date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
	}
	public static String dateToWeek(Date date) {
		String[] WEEK = { "星期天", "星期一", "星期二","星期三","星期四", "星期五", "星期六" };
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int dayIndex = calendar.get(Calendar.DAY_OF_WEEK);
		if (dayIndex < 1 || dayIndex > 7) {
			return null;
		}
		return WEEK[dayIndex - 1];
	}
	
	
	/**
	 * 日期转成特定格式的日期
	 * @param date
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public static String dateToFormat(Date date) {
		String dateStr = null;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
		String time = format.format(date);
        String[] todayString = df.format(new Date()).split(" ");
        System.out.println(todayString.toString());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date today= sdf.parse(todayString[0]);
			//昨天 86400000 = 24*60*60*1000
			if((today.getTime() - date.getTime())>0 && (today.getTime() - date.getTime())< 86400000){
				dateStr = "昨天  "+time;
			}else if((today.getTime() - date.getTime())<=0){
				dateStr = time;
			}else if((today.getTime() - date.getTime())>86400000 && (today.getTime() - date.getTime())< 604800000){
				dateStr = dateToWeek(date)+" "+time;
			}else{
				SimpleDateFormat simpleformat = new SimpleDateFormat("MM-dd HH:mm");
				dateStr = simpleformat.format(date);
			}
		} catch (ParseException e) {
		}
		return dateStr;
	}
	
	
	@SuppressLint("SimpleDateFormat")
	public static String dateToFormatAP(Date date) {
		String dateStr = null;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String[] todayString = df.format(new Date()).split(" ");
        System.out.println(todayString.toString());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date today= sdf.parse(todayString[0]);
			//昨天 86400000 = 24*60*60*1000
			if((today.getTime() - date.getTime())>0){//非今天
				SimpleDateFormat simpleformat = new SimpleDateFormat("MM-dd HH:mm");
				dateStr = simpleformat.format(date);
			}else{
				dateStr = dateToAmPm(date);
			}
		} catch (ParseException e) {
		}
		return dateStr;
	}



	/**
	 * 日期相关转换的工具类
	 *
	 */


		public static final long UNIT_SECOND = 1000;
		public static final long UNIT_MINUTE = UNIT_SECOND * 60;
		public static final long UNIT_HOUR = UNIT_MINUTE * 60;
		public static final long UNIT_DAY = UNIT_HOUR * 24;
		public static final String DATE_TIME_FORMAT_STR = "yyyy-MM-dd kk:mm:ss";
		public static final String DATE_ONLY_FORMAT_STR = "yyyy-MM-dd";
		public static final String TIME_FORMAT_STR = "kk:mm:ss";

		/**
		 * 根据时间字符串得到long型时间
		 *
		 * @param dateStr
		 * @return
		 */
		public static long getDateTime(String dateStr) {
			long result = -1;
			try {
				SimpleDateFormat ft = new SimpleDateFormat(DATE_TIME_FORMAT_STR);
				result = ft.parse(dateStr).getTime();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return result;
		}


		/**
		 * 返回两个时间串的间隔
		 *
		 * @param timeStr1
		 * @param timeStr2
		 * @return
		 */
		private static long getQuot(String timeStr1, String timeStr2) {
			long time1 = getDateTime(timeStr1);
			long time2 = getDateTime(timeStr2);
			if (time1 == -1 || time2 == -1)
				return -1;
			else
				return time1 - time2;
		}

		/**
		 * 取得与当天日期相差的天数
		 *
		 * @param time
		 * @return
		 */
		public static long getQuotDay(String time) {
			return getQuot(getTodayDateTimeStr(), time) / UNIT_DAY;
		}

		/**
		 * 取得与当前时间间隔
		 *
		 * @param time
		 * @return
		 */
		public static long getQuot(String time) {
			return getQuot(getTodayDateTimeStr(), time);
		}

		/**
		 * 得到日期时间字符串,格式为"yyyy-MM-dd kk:mm:ss"
		 *
		 * @param timeStamp
		 * @return
		 */
		public static String getLongDateTimeStr(long timeStamp) {
			return DateFormat.format(DATE_TIME_FORMAT_STR, timeStamp).toString();
		}

		/**
		 * 得到当前日期时间字符串,格式为"yyyy-MM-dd kk:mm:ss"
		 *
		 * @return
		 */
		public static String getTodayDateTimeStr() {
			return getLongDateTimeStr(new Date().getTime());
		}

		/**
		 * 判断当前日期是否是有效日期
		 *
		 * @param period
		 * @param validDay
		 * @param begDayTime
		 * @return true:有效 , false:无效
		 */
		public static boolean checkValidDay(int period, String validDay, String begDayTime) {
			if (period <= 0 || TextUtils.isEmpty(validDay) || TextUtils.isEmpty(begDayTime))
				return false;
			long dayQuot = getQuotDay(begDayTime);
			if (dayQuot < 0)
				return false;
			long dayInterval = (dayQuot % period) + 1;
//			logger.debug("Timer isValidDay: period=" + period + ",dayQuot=" + dayQuot + ", day=" + dayInterval);
			String dayIntervalStr = String.valueOf(dayInterval);
			return (validDay.indexOf(dayIntervalStr) >= 0);
		}

		/**
		 * 得到时间字符串
		 *
		 * @param timeStamp
		 * @return
		 */
		public static String getTimeStr(long timeStamp) {
			return DateFormat.format(TIME_FORMAT_STR, timeStamp).toString();
		}

		/**
		 * 得到当前时间字符串
		 *
		 * @return
		 */
		public static String getCurrentTimeStr() {
			return getTimeStr(new Date().getTime());
		}

		/**
		 * 判断时间是否在时间段之间
		 *
		 * @param time
		 * @param begTime
		 * @param endTime
		 * @return true:在时间段之间, false:不在该时间段之间
		 */
		public static boolean checkValidTime(String time, String begTime, String endTime) {
			if (time == null)
				return false;
			return time.compareTo(begTime) >= 0 && time.compareTo(endTime) <= 0;
		}

		/**
		 * 将星期进行转换，如将1转换为星期一，即2
		 *
		 * @param p
		 * @return 实际编码值，若找不到映射则返回-1
		 */
		public static final int parseWeek2Code(int p) {

			int r = -1;
			// 在这里做转码
			if (p == 1) {
				r = Calendar.MONDAY;
			} else if (p == 2) {
				r = Calendar.TUESDAY;
			} else if (p == 3) {
				r = Calendar.WEDNESDAY;
			} else if (p == 4) {
				r = Calendar.THURSDAY;
			} else if (p == 5) {
				r = Calendar.FRIDAY;
			} else if (p == 6) {
				r = Calendar.SATURDAY;
			} else if (p == 7) {
				r = Calendar.SUNDAY;
			}
			return r;
		}

		/**
		 * 获得年月日 yyyy-MM-dd
		 *
		 * @return
		 */
		public static final String getCurYearMonthDay() {
			Date date = new Date();
			return DateFormat.format(DATE_ONLY_FORMAT_STR, date).toString();
		}


}
