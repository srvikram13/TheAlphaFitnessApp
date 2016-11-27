package com.alphafitness.thealphafitnessapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;


public class MyService extends Service implements SensorEventListener {

    private static final String TAG = "DEBUG: MyService";

    IMyInterface.Stub mBinder;

    private SensorManager sensorManager;

    DBHelper dbHelper;

    /*
    http://javapapers.com/android/draw-path-on-google-maps-android-api/

     */

    public MyService() {
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Inside onCreate()");
        Toast.makeText(this, "Inside onCreate() of MyService", Toast.LENGTH_SHORT).show();

        dbHelper = DBHelper.getInstance(getApplicationContext());

        final MyService mySvc = this;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mBinder = new IMyInterface.Stub() {
            public int square (int value) throws RemoteException {
                return value * value;
            }
            public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
                                        double aDouble, String aString) {
            }

            public void startCounting() {
                Log.d(TAG, "startCounting()");
                // don't start counting if already counting
                if (!isCounting()) {
                    Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
                    if (countSensor != null) {
                        sensorManager.registerListener(mySvc, countSensor, SensorManager.SENSOR_DELAY_UI);
                        setIsCounting(true);
                        Log.d(TAG, "setIsCounting(true)");
                        long currDatetime = Calendar.getInstance().getTimeInMillis();
                        setWorkoutStartTime(currDatetime);
                    } else {
                        //Toast.makeText(mySvc, "Count sensor not available!", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Count sensor not available!");
                    }
                }
            }

            public void stopCounting() {
                Log.d(TAG, "stopCounting()");

                //if you unregister the last listener, the hardware will stop detecting step events
                sensorManager.unregisterListener(mySvc);
                // flush the count so far
                sensorManager.flush(MyService.this);
                setIsCounting(false);
                // add current workout data to all time date in DB
                storeWorkoutData();
            }
        };
    }

    /**
     * Fetches all time data from DB, adds current workout data into it and then stores it back into DB
     */
    private void storeWorkoutData() {
        Log.d(TAG, "storeWorkoutData()");

        // get current workout data from SharedPreferences
        int currStepCount = getCurrentStepCount();
        long workoutStartTime = getWorkoutStartTime();
        long currTime = Calendar.getInstance().getTimeInMillis();
        long totalWorkoutTimeInSecs = (currTime - workoutStartTime)/60;
        float currWorkoutDistance = getWorkoutDistance(currStepCount);
        int currWorkoutCalories = getWorkoutCalories(currStepCount, totalWorkoutTimeInSecs);

        StepData sd = new StepData();
        sd.timestamp = Long.toString(currTime);
        sd.calories = currWorkoutCalories;
        sd.count = currStepCount;
        dbHelper.insertStepData(sd);
        
        Log.d(TAG, "totalWorkoutTimeInSecs: " + totalWorkoutTimeInSecs);
        Log.d(TAG, "currWorkoutDistance: " + currWorkoutDistance);
        Log.d(TAG, "currWorkoutCalories: " + currWorkoutCalories);

        // get All Time Data from DB
        AllTimeData allTimeData = dbHelper.getAllTimeData();
        Log.d(TAG, "Old allTimeData: " + allTimeData.toString());

        // add current workout data to all time data
        allTimeData.noOfWorkouts++;
        allTimeData.time += totalWorkoutTimeInSecs;
        allTimeData.distance += currWorkoutDistance;
        allTimeData.calories += currWorkoutCalories;

        // set All Time Data in DB
        dbHelper.insertAllTimeData(allTimeData);
        Log.d(TAG, "New allTimeData: " + allTimeData.toString());
    }

    private int getWorkoutCalories(int currStepCount, long totalWorkoutTimeInSecs) {
        // assuming 1 step = 0.04 calories
        int calories = (int) (currStepCount * 0.04);
        return calories;
    }

    private float getWorkoutDistance(int currStepCount) {
        // 1 step = 0.762 meters (for men) & 0.67 meters (for women)
        // 1 step = 0.00047348485 miles (for men) & 0.0004163187 miles (for women)
        // assuming 1 step = 0.00044 miles
        float workoutDistance = (float) (currStepCount * 0.00044);
        return workoutDistance;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Inside onDestroy()");
        Toast.makeText(this, "Inside onDestroy() of MyService", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Inside onBind()");
        Toast.makeText(this, "Inside onBind() of MyService", Toast.LENGTH_SHORT).show();
        return mBinder;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, "Inside onSensorChanged()");
        if (isCounting()) {
            int currStepCount = (int) event.values[0];
            Log.d(TAG, "Total steps: " + String.valueOf(currStepCount));
            // store total steps count of current workout in Shared preferences
            setCurrentStepCount(currStepCount);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged, accuracy: " + accuracy);
    }

    // SharedPreferences methods

    private boolean isCounting() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("AlphaFitness", Context.MODE_PRIVATE);
        return preferences.getBoolean("IS_COUNTING", false);
    }

    private void setIsCounting(boolean isCounting) {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("AlphaFitness", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("IS_COUNTING", isCounting);
        editor.commit();
    }

    private void setCurrentStepCount(int currStepCount) {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("AlphaFitness", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("CURRENT_STEP_COUNT", currStepCount);
        editor.commit();
    }

    private int getCurrentStepCount() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("AlphaFitness", Context.MODE_PRIVATE);
        return preferences.getInt("CURRENT_STEP_COUNT", 0);
    }

    private void setWorkoutStartTime(long startTime) {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("AlphaFitness", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("WORKOUT_START_TIME", startTime);
        editor.commit();
    }

    private Long getWorkoutStartTime() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("AlphaFitness", Context.MODE_PRIVATE);
        return preferences.getLong("WORKOUT_START_TIME", -1);
    }


}
