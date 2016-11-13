package com.alphafitness.thealphafitnessapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;


public class MyService extends Service implements SensorEventListener {

    private static final String TAG = "DEBUG: MyService";

    IMyInterface.Stub mBinder;

    private SensorManager sensorManager;
    boolean isCounting;

    public MyService() {
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Inside onCreate()");
        Toast.makeText(this, "Inside onCreate() of MyService", Toast.LENGTH_SHORT).show();

        final MyService mySvc = this;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mBinder = new IMyInterface.Stub() {
            public int square (int value) throws RemoteException {
                return value * value;
            }
            public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
                                        double aDouble, String aString) {
            }

            public boolean startCounting() {
                Log.d(TAG, "startCounting()");
                // don't start counting if already counting
                if (!isCounting) {
                    Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
                    if (countSensor != null) {
                        sensorManager.registerListener(mySvc, countSensor, SensorManager.SENSOR_DELAY_UI);
                        isCounting = true;
                    } else {
                        //Toast.makeText(mySvc, "Count sensor not available!", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Count sensor not available!");
                    }
                }
                return true;
            }

            public void stopCounting() {
                Log.d(TAG, "stopCounting()");

                //if you unregister the last listener, the hardware will stop detecting step events
                sensorManager.unregisterListener(mySvc);
                isCounting = false;
            }
        };
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
        if (isCounting) {
            Log.d(TAG, "Total steps: " + String.valueOf(event.values[0]));
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged, accuracy: " + accuracy);
    }
}
