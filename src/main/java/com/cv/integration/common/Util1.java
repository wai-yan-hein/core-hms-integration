package com.cv.integration.common;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Slf4j
public class Util1 {
    public static double getDouble(Object obj) {
        return obj == null ? 0 : Double.parseDouble(obj.toString());
    }

    public static int getInteger(Object obj) {
        return obj == null ? 0 : Integer.parseInt(obj.toString());
    }

    public static boolean isNullOrEmpty(Object obj) {
        return obj == null || obj.toString().isEmpty();
    }

    public static String isNull(String input, String output) {
        return isNullOrEmpty(input) ? output : input;
    }

    public static Date getTodayDate() {
        return Calendar.getInstance().getTime();
    }

    public static Date toDate(String sqlDate) {
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(sqlDate);
        } catch (ParseException e) {
            log.error(String.format("toDate : %s", e));
        }
        return date;
    }
    public static boolean getBoolean(String obj) {
        boolean status = false;
        if (!Strings.isNullOrEmpty(obj)) {
            status = obj.equals("1") || obj.equalsIgnoreCase("true");
        }
        return status;

    }
}
