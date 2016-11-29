    // IMyInterface.aidl
package com.alphafitness.thealphafitnessapp;

// Declare any non-default types here with import statements

interface IMyInterface {
    /**
    int square(in int value);

     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.

    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
     */
    void startCounting();

    void stopCounting();

    int getCurrentWorkoutStepCount();
    long getCurrentWorkoutStartTime();

}
