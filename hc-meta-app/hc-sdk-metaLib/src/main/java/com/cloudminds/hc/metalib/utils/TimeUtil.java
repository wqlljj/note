package com.cloudminds.hc.metalib.utils;

import android.content.Context;
import android.text.format.DateUtils;


import com.cloudminds.hc.metalib.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by willzhang on 18/04/17
 */

public final class TimeUtil {

    public static final String SERVER_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String LOCAL_TIME_FORMAT = "yyyyMMdd_HHmmss";

    public static String readableTime(Context mContext, long time) {
        return formatTimeStampString(mContext, time, false);
    }

    public static String formatTimeStampString(Context mContext, long when, boolean fullFormat) {
        GregorianCalendar then = new GregorianCalendar();
        then.setTimeInMillis(when);
        GregorianCalendar now = new GregorianCalendar();
        now.setTimeInMillis(System.currentTimeMillis());

        // Basic settings for formatDateTime() we want for all cases.
        int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT |
                DateUtils.FORMAT_ABBREV_ALL |
                DateUtils.FORMAT_CAP_AMPM;

        // If the caller has asked for full details, make sure to show the date
        // and time no matter what we've determined above (but still make showing
        // the year only happen if it is a different year from today).
        if (fullFormat) {
            format_flags |= (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
        } else {
            long timeDiff = System.currentTimeMillis() - when;
            if (timeDiff < DateUtils.MINUTE_IN_MILLIS) {
                // If the message is in 1 minute, show "now"
                return mContext.getResources().getString(R.string.posted_now);
            } else if (timeDiff < DateUtils.HOUR_IN_MILLIS) {
                // If the message is 1 minute ago but in 1 hour, show x=1,2... minute(s)
                long count = (timeDiff / DateUtils.MINUTE_IN_MILLIS);
                String format = mContext.getResources().getQuantityString(
                        R.plurals.num_minutes_ago, (int) count);
                return String.format(format, count);
            } else if (then.get(GregorianCalendar.DAY_OF_YEAR) == now.get(GregorianCalendar.DAY_OF_YEAR)) {
                // If the message is 1 hour ago but in 1 day, show exactly time, sample as 15:08
                format_flags |= DateUtils.FORMAT_SHOW_TIME;
            } else if (timeDiff < DateUtils.WEEK_IN_MILLIS) {
                // If the message is 1 day ago but in 1 week, show weekday and exactly time
                format_flags |= DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_TIME;
            } else if (then.get(GregorianCalendar.YEAR) == now.get(GregorianCalendar.YEAR)) {
                // If the message is 1 week ago but in 1 year, show date and exactly time
                format_flags |= DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME;
            } else {
                // If the message is from a different year, show the date and year.
                format_flags |= DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE;
            }
        }
        return DateUtils.formatDateTime(mContext, when, format_flags);
    }

    public static long fromFormattedTimeString(String formattedTime, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        try {
            Date date = dateFormat.parse(formattedTime);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String toLocalFormat(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(LOCAL_TIME_FORMAT, Locale.getDefault());
        Date date = new Date(time);
        return dateFormat.format(date);
    }

    public static String toServerFormat(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(SERVER_TIME_FORMAT, Locale.getDefault());
        Date date = new Date(time);
        return dateFormat.format(date);
    }
}
