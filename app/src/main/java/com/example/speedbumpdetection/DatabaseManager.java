package com.example.speedbumpdetection;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;


public class DatabaseManager extends SQLiteOpenHelper {

    public final static String TABLE_NAME = "Sensor_Data_Table";
    public static final String ACC_X_COLUMN = "ACC_X";
    public static final String ACC_Y_COLUMN = "ACC_Y";
    public static final String ACC_Z_COLUMN = "ACC_Z";
    public static final String GYRO_X_COLUMN = "GYRO_X";
    public static final String GYRO_Y_COLUMN = "GYRO_Y";
    public static final String GYRO_Z_COLUMN = "GYRO_Z";
    public static final String ID_COLUMN = "ID";
    public static final String LAT_COLUMN = "Latitude";
    public static final String LON_COLUMN = "Longitude";
    public static final String ALT_COLUMN = "Altitude";
    public  static final String ROAD_ANOMALY_COLUMN = "Road_Anomaly_type";
    public static final String DATE_TIME_COLUMN = "Date";

    public DatabaseManager(@Nullable Context context) {
        super(context, "SpeedBumpSensors.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" + ID_COLUMN
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + ACC_X_COLUMN + " REAL, " + ACC_Y_COLUMN
                + " REAL, " + ACC_Z_COLUMN + " REAL, " + GYRO_X_COLUMN + " REAL, " + GYRO_Y_COLUMN
                + " REAL, " + GYRO_Z_COLUMN+ " REAL, " +LAT_COLUMN + " REAL, "+ LON_COLUMN + " REAL, "
                + ALT_COLUMN + " REAL, " + ROAD_ANOMALY_COLUMN + " TEXT(25), " + DATE_TIME_COLUMN
                + " TEXT(25))";

        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void addOne(SensorsData sensorsData){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        cv.put(ACC_X_COLUMN, sensorsData.getAccel_x());
        cv.put(ACC_Y_COLUMN, sensorsData.getAccel_y());
        cv.put(ACC_Z_COLUMN, sensorsData.getAccel_z());
        cv.put(GYRO_X_COLUMN, sensorsData.getGyro_x());
        cv.put(GYRO_Y_COLUMN, sensorsData.getGyro_y());
        cv.put(GYRO_Z_COLUMN, sensorsData.getGyro_z());
        cv.put(LAT_COLUMN, sensorsData.getLat());
        cv.put(LON_COLUMN, sensorsData.getLon());
        cv.put(ALT_COLUMN, sensorsData.getAlt());
        cv.put(ROAD_ANOMALY_COLUMN, "NONE");
        cv.put(DATE_TIME_COLUMN, formatter.format(date));

        db.insert(TABLE_NAME, null, cv);

    }


}
