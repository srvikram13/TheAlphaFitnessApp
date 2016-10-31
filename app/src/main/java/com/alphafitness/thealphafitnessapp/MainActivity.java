package com.alphafitness.thealphafitnessapp;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity implements WorkoutDetails.OnFragmentInteractionListener, RecordWorkout.OnFragmentInteractionListener{

    WorkoutDetails workoutDetails;
    RecordWorkout recordWorkout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Configuration config = getResources().getConfiguration();
        workoutDetails = new WorkoutDetails();
        recordWorkout = new RecordWorkout();

        FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();
        Log.d("DEBUG", "check this");
        if(config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            transaction.replace(R.id.fragment_container, workoutDetails);
        } else {
            transaction.replace(R.id.fragment_container, recordWorkout);
        }
        transaction.commit();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen

        FragmentTransaction transaction =
                getSupportFragmentManager().beginTransaction();

        Configuration config = getResources().getConfiguration();
        Log.d("DEBUG", "orientation changed");
        if(config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            transaction.replace(R.id.fragment_container, workoutDetails);
        } else {
            transaction.replace(R.id.fragment_container, recordWorkout);
        }
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_record_workout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }
}
