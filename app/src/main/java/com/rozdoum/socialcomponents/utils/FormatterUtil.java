/*
 * Copyright 2017 Rozdoum
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.rozdoum.socialcomponents.utils;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateUtils;

import com.rozdoum.socialcomponents.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Kristina on 10/17/16.
 */

public class FormatterUtil {

    public static String firebaseDBDate = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static String firebaseDBDay = "yyyy-MM-dd";
    public static final long Rangeof_Time = DateUtils.MINUTE_IN_MILLIS * 5; // 5 minutes

    public static String TimeAndDate = "yyyy-MM-dd HH:mm:ss";

    public static SimpleDateFormat to_retrieveFirebase_Date_Format() {
        SimpleDateFormat cbDateFormat = new SimpleDateFormat(firebaseDBDate);
        cbDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return cbDateFormat;
    }

    public static String to_formatFirebs_day(Date date) {
        SimpleDateFormat cbDateFormat = new SimpleDateFormat(firebaseDBDay);
        cbDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return cbDateFormat.format(date);
    }

    public static String to_formattime_day(Date date) {
        SimpleDateFormat cbDateFormat = new SimpleDateFormat(TimeAndDate);
        cbDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return cbDateFormat.format(date);
    }

    public static CharSequence getRelativeTimeSpanString(Context context, long time) {
        long now = System.currentTimeMillis();
        long range = Math.abs(now - time);

        if (range < Rangeof_Time) {
            return context.getString(R.string.now_time_range);
        }

        return DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
    }

    public static CharSequence getRelativeTimeSpanStringShort(Context context, long time) {
        long now = System.currentTimeMillis();
        long range = Math.abs(now - time);
        return to_duration_format(context, range, time);
    }

    private static CharSequence to_duration_format(Context context, long range, long time) {
        final Resources res = context.getResources();
        if (range >= DateUtils.WEEK_IN_MILLIS + DateUtils.DAY_IN_MILLIS) {
            return to_format_day(context, time);
        } else if (range >= DateUtils.WEEK_IN_MILLIS) {
            final int days = (int) ((range + (DateUtils.WEEK_IN_MILLIS / 2)) / DateUtils.WEEK_IN_MILLIS);
            return String.format(res.getString(R.string.duration_week_shortest), days);
        } else if (range >= DateUtils.DAY_IN_MILLIS) {
            final int days = (int) ((range + (DateUtils.DAY_IN_MILLIS / 2)) / DateUtils.DAY_IN_MILLIS);
            return String.format(res.getString(R.string.duration_days_shortest), days);
        } else if (range >= DateUtils.HOUR_IN_MILLIS) {
            final int hours = (int) ((range + (DateUtils.HOUR_IN_MILLIS / 2)) / DateUtils.HOUR_IN_MILLIS);
            return String.format(res.getString(R.string.duration_hours_shortest), hours);
        } else if (range >= Rangeof_Time) {
            final int minutes = (int) ((range + (DateUtils.MINUTE_IN_MILLIS / 2)) / DateUtils.MINUTE_IN_MILLIS);
            return String.format(res.getString(R.string.duration_minutes_shortest), minutes);
        } else {
            return res.getString(R.string.now_time_range);
        }
    }

    private static String to_format_day(Context context, long time) {
        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH;
        Formatter f = new Formatter(new StringBuilder(50), Locale.getDefault());
        return DateUtils.formatDateRange(context, f, time, time, flags).toString();
    }
}
