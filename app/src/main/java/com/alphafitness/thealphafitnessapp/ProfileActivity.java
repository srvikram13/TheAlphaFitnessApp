package com.alphafitness.thealphafitnessapp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ProfileActivity extends AppCompatActivity implements OnItemSelectedListener {

    private static final String TAG = "DEBUG: ProfileActivity";

    DBHelper dbHelper;

    private Spinner gender;
    private Button btnSubmit;

    private EditText weight;
    private EditText username;

    private boolean genderSetFromApp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = DBHelper.getInstance(getApplicationContext());

        // add listener to gender spinner
        gender = (Spinner)findViewById(R.id.user_gender);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gender_array, android.R.layout.simple_spinner_item);
        //adapter.setDropDownViewResource();
        gender.setAdapter(adapter);
        gender.setOnItemSelectedListener(this);

        // textWatcher to be used for username and weight edittexts
        TextWatcher textWatcher = new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                // make submit button visible
                btnSubmit.setVisibility(View.VISIBLE);
            }
        };

        // add listener to weight editText
        weight = (EditText) findViewById(R.id.user_weight);
        weight.addTextChangedListener(textWatcher);

        // add listener to username editText
        username = (EditText) findViewById(R.id.username);
        username.addTextChangedListener(textWatcher);

        // add listener to submit button
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnSubmit.setVisibility(View.GONE);

        btnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String genderStr = String.valueOf(gender.getSelectedItem());
                String usernameStr = username.getText().toString();
                String wtStr = weight.getText().toString();

                //Log.d(TAG, "usernameStr == \"\": " + (usernameStr == ""));
                //Log.d(TAG, "usernameStr.equalsIgnoreCase(\"\"): " + usernameStr.equalsIgnoreCase(""));

                float wt = 0;
                try {
                    wt = Float.parseFloat(wtStr);
                }
                catch (Exception ex){
                    Toast.makeText(ProfileActivity.this, "Please input valid weight between [50, 500] lbs.", Toast.LENGTH_SHORT).show();
                }
                if (wt < 50  || wt > 500) {
                    Toast.makeText(ProfileActivity.this, "Please input valid weight between [50, 500] lbs.", Toast.LENGTH_SHORT).show();
                }
                else
                if (usernameStr.equalsIgnoreCase("")) {
                    Toast.makeText(ProfileActivity.this, "Please input valid username.", Toast.LENGTH_SHORT).show();
                }
                else {
                    // store in db and then hide submit btn
                    Info info = new Info();
                    info.gender = genderStr;
                    info.username = usernameStr;
                    info.weight = wt;
                    dbHelper.updateInfo(info);
                    Log.d(TAG, "DEBUG: Updated info: " + info.toString());
                    btnSubmit.setVisibility(View.GONE);
                }
                /*Toast.makeText(ProfileActivity.this,
                        "OnClickListener : " +
                                "\nGender : "+ genderStr +
                                "\nUsername : "+ usernameStr +
                                "\nWeight : "+ wtStr,
                        Toast.LENGTH_SHORT).show();
*/
                Log.d(TAG, "DEBUG: OnClickListener : " +
                        "\nGender : "+ genderStr +
                        "\nUsername : "+ usernameStr +
                        "\nWeight : "+ wtStr);
            }
        });

        // get info and set weight, gender values
        Info info = dbHelper.getInfo();
        if (info.weight != 0) {
            weight.setText(String.valueOf(info.weight));
        }
        if (info.username != "") {
            username.setText(info.username);
        }
        if (info.gender != null && info.gender != "") {
            int pos = adapter.getPosition(info.gender);
            gender.setSelection(pos);
            //btnSubmit.setVisibility(View.GONE);
            genderSetFromApp = true;
        }

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

    // on item selected listener for spinner 'gender'
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        //String item = parent.getItemAtPosition(pos);
        Toast.makeText(parent.getContext(),
                "OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString(),
                Toast.LENGTH_SHORT).show();

        Log.d(TAG, "DEBUG: OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString());

        if (genderSetFromApp){
            // user did not set it
            genderSetFromApp = false;
        }else {
            // make submit button visible
            btnSubmit.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnSubmit.setVisibility(View.GONE);
        AllTimeData allTimeData = dbHelper.getAllTimeData();
        populateAllTimeData(allTimeData);
        populateWeeklyAverageData(allTimeData);
    }

    private void populateWeeklyAverageData(AllTimeData allTimeData) {
        // get initial start time of app and calculate noOfWeeks
        Date startTime = dbHelper.getInfo().startTime;
        Log.d(TAG, "DEBUG: populateWeeklyAverageData(), info.startTime: " + dbHelper.getInfo().startTime);
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