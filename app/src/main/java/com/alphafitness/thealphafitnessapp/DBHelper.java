package com.alphafitness.thealphafitnessapp;

import static android.provider.BaseColumns._ID;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DEBUG: DBHelper";

    private static final String DATABASE_NAME = "alphafitness.db";
    private static final int DATABASE_VERSION = 6;

    public static final String TABLE_ALL_TIME = "alltime";
    // Columns in the 'alltime' table
    public static final String ALL_TIME_KEY = "key";
    public static final String ALL_TIME_DISTANCE = "distance";
    public static final String ALL_TIME_TIME = "time";
    public static final String ALL_TIME_NUM_OF_WORKOUTS = "noofworkouts";
    public static final String ALL_TIME_CALORIES = "calories";

    public static final String TABLE_STEPS = "steps";
    // Columns in the 'steps' table
    public static final String STEPS_TIMESTAMP = "timestamp";
    public static final String STEPS_COUNT = "count";
    public static final String STEPS_CALORIES = "calories";

    public static final String TABLE_INFO = "info";
    // Columns in the 'info' table
    public static final String INFO_KEY = "key";
    public static final String INFO_START_TIME = "starttime";   // start time of the first workout
    public static final String INFO_USERNAME = "username";
    public static final String INFO_GENDER = "gender";
    public static final String INFO_WEIGHT = "weight";

    public static final String TABLE_WORKOUT_PATH = "workoutpath";
    // Columns in the 'workoutpath' table
    public static final String WORKOUT_PATH_LATITUDE = "latitude";
    public static final String WORKOUT_PATH_LONGITUDE = "longitude";

    private static DBHelper mInstance = null;
    public static DBHelper getInstance(Context ctx) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (mInstance == null) {
            mInstance = new DBHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    private DBHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_ALL_TIME + " ("
                + ALL_TIME_KEY + " TEXT NOT NULL, "
                + ALL_TIME_DISTANCE + " REAL NOT NULL, "
                + ALL_TIME_TIME + " INTEGER NOT NULL, "
                + ALL_TIME_NUM_OF_WORKOUTS + " INTEGER NOT NULL, "
                + ALL_TIME_CALORIES + " INTEGER NOT NULL);");

        db.execSQL("CREATE TABLE " + TABLE_STEPS + " ("
                + STEPS_TIMESTAMP + " TEXT NOT NULL, "
                + STEPS_COUNT + " INTEGER NOT NULL, "
                + STEPS_CALORIES + " INTEGER NOT NULL);");

        db.execSQL("CREATE TABLE " + TABLE_INFO + " ("
                + INFO_KEY + " TEXT NOT NULL, "
                + INFO_START_TIME + " TEXT NOT NULL, "
                + INFO_USERNAME + " TEXT, "
                + INFO_GENDER + " TEXT NOT NULL, "
                + INFO_WEIGHT + " REAL NOT NULL);");

        db.execSQL("CREATE TABLE " + TABLE_WORKOUT_PATH + " ("
                + WORKOUT_PATH_LATITUDE + " REAL NOT NULL, "
                + WORKOUT_PATH_LONGITUDE + " REAL NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // When Android detects you’re referencing an old database (based on
        // the version number), it will call the onUpgrade( ) method
        deleteAllTablesAndReCreate(db);
    }

    private void deleteAllTablesAndReCreate(SQLiteDatabase db) {
        try {
            db.execSQL("DROP TABLE " + TABLE_ALL_TIME + ";");
            db.execSQL("DROP TABLE " + TABLE_STEPS + ";");
            db.execSQL("DROP TABLE " + TABLE_INFO + ";");
            db.execSQL("DROP TABLE " + TABLE_WORKOUT_PATH + ";");
        }catch (SQLiteException e) {

        } finally {
            onCreate(db);
        }
    }
    /**
     * update workout path in 'workout path' table
     * @param latitude, longitude
     */
    public void updateWorkoutPath(double latitude, double longitude) {
        SQLiteDatabase db  = this.getReadableDatabase();
        db.execSQL("INSERT INTO " + TABLE_WORKOUT_PATH+ " VALUES ('" + latitude + "', " + longitude + ")");
        db.close();
    }
    public void clearWorkoutPath() {
        SQLiteDatabase db  = this.getReadableDatabase();
        db.execSQL("DROP TABLE " + TABLE_WORKOUT_PATH + ";");

        db.execSQL("CREATE TABLE " + TABLE_WORKOUT_PATH + " ("
                + WORKOUT_PATH_LATITUDE + " REAL NOT NULL, "
                + WORKOUT_PATH_LONGITUDE + " REAL NOT NULL);");

        db.close();
    }
    /**
     * Get all location coordinates as (latitude,longitude) from 'workout path' table.
     * @return list of lat, long objects
     */

    public List<LatLng> getWorkoutPath() {
        String selectQuery = "SELECT " + WORKOUT_PATH_LATITUDE + ", "+ WORKOUT_PATH_LONGITUDE + " FROM " + TABLE_WORKOUT_PATH;
        SQLiteDatabase db  = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        List<LatLng> data = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                // get the data into array, or class variable
                LatLng location = new LatLng(cursor.getDouble(cursor.getColumnIndex(WORKOUT_PATH_LATITUDE)), cursor.getDouble(cursor.getColumnIndex(WORKOUT_PATH_LONGITUDE)));
                data.add(location);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return data;
    }

    /**
     * Insert StepData into 'steps' table
     * @param stepData
     */
    public void insertStepData(StepData stepData) {
        Log.d(TAG, "in insertStepData"+stepData.toString());
        SQLiteDatabase db  = this.getReadableDatabase();
        db.execSQL("INSERT INTO " + TABLE_STEPS + " VALUES ('" + stepData.timestamp + "', " + stepData.count + ", " + stepData.calories + ")");
        db.close();
    }

    /**
     * Get all records (timestamp, count of steps in last 5 mins) from 'steps' table.
     * @return list of StepData objects
     */
    public List<StepData> getStepsData() {
        String selectQuery = "SELECT " + STEPS_TIMESTAMP + ", " + STEPS_COUNT + ", " + STEPS_CALORIES + " FROM " + TABLE_STEPS;
        SQLiteDatabase db  = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        List<StepData> data = new ArrayList<StepData>();
        if (cursor.moveToFirst()) {
            do {
                // get the data into array, or class variable
                StepData currData = new StepData();
                currData.timestamp = cursor.getString(cursor.getColumnIndex(STEPS_TIMESTAMP));
                currData.count = cursor.getInt(cursor.getColumnIndex(STEPS_COUNT));
                currData.calories = cursor.getInt(cursor.getColumnIndex(STEPS_CALORIES));
                data.add(currData);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return data;
    }

    /**
     * Insert AllTimeData into 'alltime' table
     * @param allTimeData
     */
    public void insertAllTimeData(AllTimeData allTimeData) {
        SQLiteDatabase db  = this.getReadableDatabase();
        db.execSQL("INSERT INTO " + TABLE_ALL_TIME + " VALUES ('ALLTIME', " + allTimeData.distance + ", " + allTimeData.time + ", " + allTimeData.noOfWorkouts + ", " + allTimeData.calories + ")");
        db.close();
    }

    /**
     * Updates AllTimeData into 'alltime' table
     * @param allTimeData
     */
    public void updateAllTimeData(AllTimeData allTimeData) {
        SQLiteDatabase db  = this.getReadableDatabase();
        db.execSQL("UPDATE " + TABLE_ALL_TIME + " SET " + ALL_TIME_DISTANCE + " = " + allTimeData.distance
                + ", " + ALL_TIME_TIME + " = " + allTimeData.time + ", " + ALL_TIME_NUM_OF_WORKOUTS + " = " + allTimeData.noOfWorkouts + ", "
                + ALL_TIME_CALORIES + " = " + allTimeData.calories
                + " WHERE " + ALL_TIME_KEY + " = 'ALLTIME'");
        db.close();
    }

    /**
     * Get the all time data from 'alltime' table
     * @return AllTimeData object
     */
    public AllTimeData getAllTimeData() {
        String selectQuery = "SELECT * FROM " + TABLE_ALL_TIME;
        SQLiteDatabase db  = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        AllTimeData data = new AllTimeData();
        if (cursor.moveToFirst()) {
            // get the data into class variable
            data.distance = cursor.getFloat(cursor.getColumnIndex(ALL_TIME_DISTANCE));
            data.time = cursor.getInt(cursor.getColumnIndex(ALL_TIME_TIME));
            data.noOfWorkouts = cursor.getInt(cursor.getColumnIndex(ALL_TIME_NUM_OF_WORKOUTS));
            data.calories = cursor.getInt(cursor.getColumnIndex(ALL_TIME_CALORIES));
        }
        cursor.close();
        db.close();
        return data;
    }

    /**
     * Insert Info into 'info' table
     * @param info
     */
    public void insertInfo(Info info) {
        SQLiteDatabase db  = this.getReadableDatabase();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startTimeStr = df.format(info.startTime);
        db.execSQL("INSERT INTO " + TABLE_INFO + " VALUES ('INFO', '" + startTimeStr + "', '" + info.username + "', '" + info.gender + "', " + info.weight + ")");
        db.close();
    }

    /**
     * Only updates gender and weight
     * @param info
     */
    public void updateInfo(Info info) {
        SQLiteDatabase db  = this.getReadableDatabase();
        //DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //String startTimeStr = df.format(info.startTime);
        db.execSQL("UPDATE " + TABLE_INFO + " SET " + INFO_GENDER + " = '" + info.gender + "', " + INFO_USERNAME + " = '" + info.username + "', " + INFO_WEIGHT + " = " + info.weight
                + " WHERE " + INFO_KEY + " = 'INFO'");
        db.close();
    }

    /**
     * Get data from 'info' table
     * @return Info object
     */
    public Info getInfo() {
        String selectQuery = "SELECT * FROM " + TABLE_INFO;
        SQLiteDatabase db  = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        Info data = new Info();
        if (cursor.moveToFirst()) {
            // get the data into class variable
            String startTimeStr = cursor.getString(cursor.getColumnIndex(INFO_START_TIME));
            try {
                data.startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startTimeStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String genderStr = cursor.getString(cursor.getColumnIndex(INFO_GENDER));
            String usernameStr = cursor.getString(cursor.getColumnIndex(INFO_USERNAME));
            float weight = cursor.getFloat(cursor.getColumnIndex(INFO_WEIGHT));
            data.gender = genderStr;
            data.username = usernameStr;
            data.weight = weight;
        }
        cursor.close();
        db.close();
        if (data.startTime == null) {
            // set start time
            data.startTime = new Date();
            data.gender = "Male";
            data.username = "";
            data.weight = 0;
            insertInfo(data);
        }
        return data;
    }
}

class StepData {
    String timestamp;
    int count;
    int calories;

    @Override
    public String toString() {
        String retStr = "{ timestamp: " + timestamp
                + ", count: " + count
                + ", calories: " + calories + " }";
        return retStr;
    }
}

class AllTimeData {
    float distance;
    int time;
    int noOfWorkouts;
    int calories;

    @Override
    public String toString() {
        String retStr = "{ noOfWorkouts: " + noOfWorkouts
                + ", distance: " + distance
                + ", time: " + time
                + ", calories: " + calories + " }";
        return retStr;
    }
}

class Info {
    Date startTime;
    String username;
    String gender;
    float weight;

    @Override
    public String toString() {
        String retStr = "{ startTime: " + startTime
                + ", username: " + username
                + ", gender: " + gender
                + ", weight: " + weight + " }";
        return retStr;
    }
}