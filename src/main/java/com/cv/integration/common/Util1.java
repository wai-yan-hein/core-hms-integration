package com.cv.integration.common;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;


@Slf4j
public class Util1 {
    private static final DecimalFormat df0 = new DecimalFormat("0");

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

    public static String toDateStr(LocalDateTime date) {
        if (date != null) {
            // Define the formatter for the desired date format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // Format the LocalDateTime to a String
            return date.format(formatter);
        }
        return null;
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

    public static boolean getBoolean(Object obj) {
        if (obj == null) {
            return false;
        }
        return obj.toString().equals("1") || obj.toString().equalsIgnoreCase("true");

    }

    public static String toDateStr(Date date, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        String strDate = null;
        try {
            strDate = formatter.format(date);
        } catch (Exception ex) {
            System.out.println("toDateStr Error : " + ex.getMessage());
        }

        return strDate;
    }

    public static byte[] writeJsonFile(Object data, String exportPath) throws IOException {
        try (Writer writer = new FileWriter(exportPath, StandardCharsets.UTF_8)) {
            //Gson gson = new GsonBuilder().serializeNulls().create();
            Gson gson = new Gson();
            gson.toJson(data, writer);
        }
        return IOUtils.toByteArray(new FileInputStream(exportPath));
    }

    public static Date convertToDate(LocalDateTime localDateTime) {
        if (localDateTime != null) {
            ZoneId zoneId = ZoneId.systemDefault();
            ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);

            // Set the time to midnight (00:00:00)
            zonedDateTime = zonedDateTime.with(LocalTime.MIDNIGHT);

            return Date.from(zonedDateTime.toInstant());
        }
        return null;
    }

    public static Date toDate(String str, String format) {
        if (str != null) {
            DateFormat dateFormat = new SimpleDateFormat(format);
            try {
                return dateFormat.parse(str);
            } catch (ParseException e) {
                log.error("toDate : " + e.getMessage());
            }
        }
        return null;
    }

    public static boolean compareDate(LocalDateTime dateTime, String syncDate) {
        Date d1 = convertToDate(dateTime);
        Date d2 = toDate(syncDate, "yyyy-MM-dd");
        return d1.compareTo(d2) >= 0;
    }
}
