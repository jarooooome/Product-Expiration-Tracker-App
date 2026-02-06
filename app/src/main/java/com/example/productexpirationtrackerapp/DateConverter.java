package com.example.productexpirationtrackerapp;

import androidx.room.TypeConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateConverter {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @TypeConverter
    public static Date fromTimestamp(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return dateFormat.parse(value);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @TypeConverter
    public static String dateToTimestamp(Date date) {
        if (date == null) {
            return null;
        }
        return dateFormat.format(date);
    }
}