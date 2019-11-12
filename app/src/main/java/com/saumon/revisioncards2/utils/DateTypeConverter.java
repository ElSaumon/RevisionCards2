package com.saumon.revisioncards2.utils;

import androidx.room.TypeConverter;

import java.util.Date;

public class DateTypeConverter {
    @TypeConverter
    public static Date toDate(Long value) {
        return null == value ? null : new Date(value);
    }

    @TypeConverter
    public static Long toLong(Date value) {
        return null == value ? null : value.getTime();
    }
}
