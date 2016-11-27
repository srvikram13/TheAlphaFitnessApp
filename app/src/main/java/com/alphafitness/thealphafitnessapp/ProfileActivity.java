package com.alphafitness.thealphafitnessapp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ProfileActivity extends AppCompatActivity {

    DBHelper dbHelper;

    private EditText weight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = DBHelper.getInstance(getApplicationContext());

        Spinner gender = (Spinner)findViewById(R.id.user_gender);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gender_array, android.R.layout.simple_spinner_item);

        weight = (EditText) findViewById(R.id.user_weight);
        InputFilterMinMax filter = new InputFilterMinMax("50", "499") {};
        weight.setFilters(new InputFilter[]{filter});
        weight.setText("180");
        //adapter.setDropDownViewResource();

        gender.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        AllTimeData allTimeData = dbHelper.getAllTimeData();
        populateAllTimeData(allTimeData);
        populateWeeklyAverageData(allTimeData);
    }

    private void populateWeeklyAverageData(AllTimeData allTimeData) {
        // get initial start time of app and calculate noOfWeeks
        Date startTime = dbHelper.getInfo().startTime;
        Date currTime = new Date();
        long diff = currTime.getTime() - startTime.getTime();
        int noOfWeeks = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)/7;
        // consider current week as well
        noOfWeeks++;

        TextView weeklyAvgDistance = (TextView) findViewById(R.id.distance_walked);
        weeklyAvgDistance.setText(allTimeData.distance/noOfWeeks + " mi");

        TextView weeklyAvgTotalTime = (TextView) findViewById(R.id.time_walked);
        weeklyAvgTotalTime.setText(convertTimeDurationToStr(allTimeData.time/noOfWeeks));

        TextView weeklyAvgWorkouts = (TextView) findViewById(R.id.workout_count);
        weeklyAvgWorkouts.setText(allTimeData.noOfWorkouts/noOfWeeks + " times");

        TextView weeklyAvgCalories = (TextView) findViewById(R.id.calories);
        weeklyAvgCalories.setText(allTimeData.calories/noOfWeeks + " Cal");

    }

    private void populateAllTimeData(AllTimeData allTimeData) {

        TextView allTimeDistance = (TextView) findViewById(R.id.all_distance_walked);
        allTimeDistance.setText(allTimeData.distance + " mi");

        TextView allTimeTotalTime = (TextView) findViewById(R.id.all_time_walked);
        allTimeTotalTime.setText(convertTimeDurationToStr(allTimeData.time));

        TextView allTimeWorkouts = (TextView) findViewById(R.id.all_workout);
        allTimeWorkouts.setText(allTimeData.noOfWorkouts + " times");

        TextView allTimeCalories = (TextView) findViewById(R.id.all_calories);
        allTimeCalories.setText(allTimeData.calories + " Cal");
    }

    /**
     * Convert a duration in seconds to a string format
     */
    public static String convertTimeDurationToStr(long secs)
    {
        long days = TimeUnit.SECONDS.toDays(secs);
        secs -= TimeUnit.DAYS.toSeconds(days);
        long hours = TimeUnit.SECONDS.toHours(secs);
        secs -= TimeUnit.HOURS.toSeconds(hours);
        long minutes = TimeUnit.SECONDS.toMinutes(secs);
        secs -= TimeUnit.MINUTES.toSeconds(minutes);

        StringBuilder sb = new StringBuilder();
        sb.append(days);
        sb.append(" day(s) ");
        sb.append(hours);
        sb.append(" hr(s) ");
        sb.append(minutes);
        sb.append(" min(s) ");
        sb.append(secs);
        sb.append(" sec(s)");

        return(sb.toString());
    }
}