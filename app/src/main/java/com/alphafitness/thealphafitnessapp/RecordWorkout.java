package com.alphafitness.thealphafitnessapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import static android.content.Context.BIND_AUTO_CREATE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RecordWorkout.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RecordWorkout#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecordWorkout extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "DEBUG: RecordWorkout";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private View mView;
    private MapView mMapView;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;


    private DBHelper dbHelper;
    IMyInterface myService;
    RemoteConnection remoteConnection = null;


    private static final LatLng LOWER_MANHATTAN = new LatLng(40.722543, -73.998585);
    private static final LatLng TIMES_SQUARE = new LatLng(40.7577, -73.9857);
    private static final LatLng BROOKLYN_BRIDGE = new LatLng(40.7057, -73.9964);

    class RemoteConnection implements ServiceConnection {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Following the example above for an AIDL interface,
            // this gets an instance of the IRemoteInterface, which we can use to call on the service
            myService = IMyInterface.Stub.asInterface(service);
            Log.d(TAG, "Connected to MyService.");
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "Service has unexpectedly disconnected");
            myService = null;
        }
    }

    /*
    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Following the example above for an AIDL interface,
            // this gets an instance of the IRemoteInterface, which we can use to call on the service
            myService = IMyInterface.Stub.asInterface(service);
            Log.d(TAG, "Connected to MyService.");
        }
        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "Service has unexpectedly disconnected");
            myService = null;
        }
    };
*/
    public RecordWorkout() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RecordWorkout.
     */
    // TODO: Rename and change types and number of parameters
    public static RecordWorkout newInstance(String param1, String param2) {
        RecordWorkout fragment = new RecordWorkout();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // initialize the service
        remoteConnection = new RemoteConnection();
        Intent intent = new Intent();
        intent.setClassName("com.alphafitness.thealphafitnessapp", com.alphafitness.thealphafitnessapp.MyService.class.getName());
        if (!getContext().bindService(intent, remoteConnection, BIND_AUTO_CREATE)) {
            Toast.makeText(getContext(), "Failed to bind the remote service, MyService.", Toast.LENGTH_SHORT).show();
        }
        dbHelper = DBHelper.getInstance(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_record_workout, container, false);

        final Button btnWorkout = (Button) view.findViewById(R.id.btn_record_workout);

        if(isCounting()) {
            btnWorkout.setText(R.string.stop_workout);
        } else {
            btnWorkout.setText(R.string.start_workout);
        }
        btnWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d(TAG, "getString(R.string.start_workout): " + getString(R.string.start_workout));
                //boolean test = btnWorkout.getText() == getString(R.string.start_workout);
                //Log.d(TAG, "test: " + test);
                //btnWorkout.setText(R.string.stop_workout);

                if (myService == null) {
                    Log.d(TAG, "Not connected to MyService yet.");
                    Toast.makeText(getContext(), "Not connected to MyService yet.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (btnWorkout.getText() == getString(R.string.start_workout)){
                    // start counting
                    try {
                        setIsCounting(true);
                        myService.startCounting();
                        Log.d(TAG, "Calling startCounting().");
                    } catch (RemoteException e) {
                        Log.e(TAG, "Error occured in MyService while trying to start counting.");
                        e.printStackTrace();
                    }
                    btnWorkout.setText(R.string.stop_workout);
                } else {
                    // stop counting
                    try {
                        setIsCounting(false);
                        Log.d(TAG, "Calling stopCounting().");
                        myService.stopCounting();
                    } catch (RemoteException e) {
                        Log.e(TAG, "Error occurred in MyService while trying to stop counting.");
                        e.printStackTrace();
                    }
                    btnWorkout.setText(R.string.start_workout);
                }

            }
        });
        final ImageButton btnProfile = (ImageButton) view.findViewById(R.id.profileButton);
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), ProfileActivity.class));
            }
        });
        mView = view;
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView = (MapView) mView.findViewById(R.id.map);
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if(ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Permission to access device GPS denied.", Toast.LENGTH_LONG).show();
            return;
        }

        dbHelper.updateWorkoutPath(40.722543, -73.998585);
        dbHelper.updateWorkoutPath(40.7577, -73.9857);
        dbHelper.updateWorkoutPath(40.7057, -73.9964);

        List<LatLng> path = dbHelper.getWorkoutPath();

        if(path.size() >= 1) {
            for (int i = 0; i < path.size() - 1; i++) {
                LatLng src = path.get(i);
                LatLng dest = path.get(i + 1);

                // mMap is the Map Object
                Polyline line = googleMap.addPolyline(
                        new PolylineOptions().add(
                                new LatLng(src.latitude, src.longitude),
                                new LatLng(dest.latitude,dest.longitude)
                        ).width(2).color(Color.BLUE).geodesic(true)
                );
            }
            // move camera to zoom on map
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(path.get(0), 13));
        }

        /*
        LocationManager locationManager;
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String locationProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(locationProvider);
        if(location != null) {
            Log.d(TAG, " location: "+location.toString());
            googleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Marker"));
            // Move the camera instantly to hamburg with a zoom of 15.
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
        }
        googleMap.addPolyline((new PolylineOptions()).add(TIMES_SQUARE, BROOKLYN_BRIDGE, LOWER_MANHATTAN, TIMES_SQUARE).width(5).color(Color.BLUE).geodesic(true));
        // move camera to zoom on map
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LOWER_MANHATTAN, 13));*/
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "Inside onResume(), isCounting(): " + isCounting());
        super.onResume();
        final Button btnWorkout = (Button) getView().findViewById(R.id.btn_record_workout);
        if (isCounting()) {
            btnWorkout.setText(R.string.stop_workout);
        } else {
            btnWorkout.setText(R.string.start_workout);
        }
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }


    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();

        // unbind service
        getContext().unbindService(remoteConnection);
        remoteConnection = null;

        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private boolean isCounting() {
        SharedPreferences preferences = getContext().getSharedPreferences("AlphaFitness", Context.MODE_PRIVATE);
        return preferences.getBoolean("IS_COUNTING", false);
    }

    private void setIsCounting(boolean isCounting) {
        SharedPreferences preferences = getContext().getSharedPreferences("AlphaFitness", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("IS_COUNTING", isCounting);
        editor.commit();
    }
}