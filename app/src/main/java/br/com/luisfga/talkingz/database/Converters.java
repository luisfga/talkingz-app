package br.com.luisfga.talkingz.database;

import androidx.room.TypeConverter;

import java.sql.Timestamp;
import java.util.UUID;

public class Converters {

    @TypeConverter
    public static Long fromTimestamp(Timestamp value) {
        return value == null ? null : value.getTime();
    }

    @TypeConverter
    public static Timestamp longToTimestamp(Long value) {
        return value == null ? null : new Timestamp(value);
    }

//    @TypeConverter
//    public static Date fromLong(Long value) {
//        return value == null ? null : new Date(value);
//    }
//
//    @TypeConverter
//    public static Long dateToLong(Date date) {
//        return date == null ? null : date.getTime();
//    }

    @TypeConverter
    public static UUID fromString(String value) { return value == null ? null : UUID.fromString(value); }

    @TypeConverter
    public static String uuidToString(UUID uuid) { return uuid == null ? null : uuid.toString(); }

//    @TypeConverter
//    public static Byte[] toByteObjects(byte[] bytes) {
//        if(bytes !=null) {
//            Byte[] newArray = new Byte[bytes.length];
//
//            int i = 0;
//            for (byte b: bytes)
//                newArray[i++] = b;
//
//            return newArray;
//        }
//        return null;
//    }
//
//    @TypeConverter
//    public static byte[] toBytePrimitives(Byte[] bytes) {
//        if (bytes != null) {
//            byte[] newArray = new byte[bytes.length];
//
//            int i = 0;
//            for (byte b: bytes)
//                newArray[i++] = b;
//
//            return newArray;
//        }
//        return null;
//    }
}
