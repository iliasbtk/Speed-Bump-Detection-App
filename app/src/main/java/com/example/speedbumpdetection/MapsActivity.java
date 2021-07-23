package com.example.speedbumpdetection;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.speedbumpdetection.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
        , GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener
        , GoogleMap.OnPolylineClickListener, GoogleMap.OnMapClickListener
        , GoogleMap.OnInfoWindowClickListener{

    TextView txt_route_info;
    EditText input_search;
    Button btn_start_nav, btn_stop_nav;

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private static final int PERMISSION_FINE_LOCATION = 99;
    // How often the location check occur (Milliseconds)
    public static final int UPDATE_INTERVAL = 1;
    // How often the location check occur when maximum power is used (Seconds)
    public static final int UPDATE_FASTEST_INTERVAL = 1;

    //Config file for FusedLocationProviderClient
    LocationRequest locationRequest;

    //Google API for GPS location services
    FusedLocationProviderClient fusedLocationProviderClient;

    LocationCallback locationCallback;

    //Define sensor manager and accelerometer
    SensorManager mSensorManager;
    Sensor mAccelerometer;
    Sensor mGyroScope;

    //Variables to store collected GPS data
    double latitude, longitude, altitude;

    //Variables to store accelerometer data
    float accel_x, accel_y, accel_z, gyro_x, gyro_y, gyro_z;
    //Temporary
    TextView txt_accel_x, txt_accel_y, txt_accel_z, txt_lat, txt_lon, txt_alt, txt_accuracy,
            txt_speed, txt_bearing, txt_gyro_x, txt_gyro_y, txt_gyro_z;



    //User Location
    private Location userLocation;
    private LatLng userLatLng;



    //Speed Bumps Markers
    ArrayList<LatLng> speedBumpsList = new ArrayList<>();
    LatLng speedBump1 = new LatLng(35.7296019, -0.5876281);
    LatLng speedBump2 = new LatLng(35.7302622, -0.5876606);
    LatLng speedBump3 = new LatLng(35.7291512, -0.5856882);
    MarkerOptions speedBumpMarkerOptions;

    ArrayList<LatLng> speedBumpsListRounded = new ArrayList<>();
    LatLng speedBumpR1 = new LatLng(35.7296, -0.5876);
    LatLng speedBumpR2 = new LatLng(35.7303, -0.5877);
    LatLng speedBumpR3 = new LatLng(35.7291, -0.5857);


    Geocoder geocoder;

    //Directions
    GeoApiContext geoApiContext = null;

    ArrayList<PolylineData> polylineDataList = new ArrayList<>();

    ArrayList<LatLng> markersPositionsList = new ArrayList<>();
    ArrayList<Marker> markersList = new ArrayList<>();
    ArrayList<Marker> destinationMarkersList = new ArrayList<>();

    ArrayList<SensorsData> datasetSamples = new ArrayList<>();

    DatabaseManager databaseManager =new DatabaseManager(MapsActivity.this);




    private SensorEventListener sensorEventListenerAcc = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            accel_x = sensorEvent.values[0];
            accel_y = sensorEvent.values[1];
            accel_z = sensorEvent.values[2];

            txt_accel_x.setText(String.format("X: %s", accel_x));
            txt_accel_y.setText(String.format("Y: %s", accel_y));
            txt_accel_z.setText(String.format("Z: %s", accel_z));

            databaseManager.addOne(storeSensorsData());
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    private SensorEventListener sensorEventListenerGyro = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            gyro_x = sensorEvent.values[0];
            gyro_y = sensorEvent.values[1];
            gyro_z = sensorEvent.values[2];

            txt_gyro_x.setText(String.format("X: %s", gyro_x));
            txt_gyro_y.setText(String.format("Y: %s", gyro_y));
            txt_gyro_z.setText(String.format("Z: %s", gyro_z));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };


    








    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        txt_route_info = findViewById(R.id.txt_route_info);
        input_search = findViewById(R.id.input_search);
        btn_start_nav = findViewById(R.id.btn_start_nav);
        btn_stop_nav = findViewById(R.id.btn_stop_nav);

        // Initialize sensor manager and accelerometer
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroScope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        txt_accel_x = findViewById(R.id.txt_accel_x);
        txt_accel_y = findViewById(R.id.txt_accel_y);
        txt_accel_z = findViewById(R.id.txt_accel_z);

        txt_lat = findViewById(R.id.txt_lat);
        txt_lon = findViewById(R.id.txt_lon);
        txt_alt = findViewById(R.id.txt_alt);

        txt_gyro_x = findViewById(R.id.txt_gyro_x);
        txt_gyro_y = findViewById(R.id.txt_gyro_y);
        txt_gyro_z = findViewById(R.id.txt_gyro_z);

        // Set LocationRequest's properties
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10 * UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000 * UPDATE_FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        // Event triggered when the update interval is met
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                updateLocationValues(locationResult.getLastLocation());
            }
        };


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        input_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_SEARCH
                        || i == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == keyEvent.ACTION_DOWN
                        || keyEvent.getAction() == keyEvent.KEYCODE_ENTER){
                    findGeoLocation();

                }
                return false;
            }
        });

        getSpeedBumpsLocation();

        //Add speed bumps locations
        for(int i=0; i<speedBumpsList.size();i++){
            speedBumpMarkerOptions = new MarkerOptions()
                    .position(speedBumpsList.get(i))
                    .title("Speed Bump");
            speedBumpMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.speed_bump));
            mMap.addMarker(speedBumpMarkerOptions);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableLocation();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
            }
        }
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnPolylineClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        if(geoApiContext == null){
            geoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_maps_key))
                    .build();
        }
    }

    private void findGeoLocation() {
        String searchInput = input_search.getText().toString();
        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> addresses = new ArrayList<>();
        try{
            addresses =geocoder.getFromLocationName(searchInput, 1);
        }catch(IOException e){

        }
        if(addresses.size()>0){
            Address address = addresses.get(0);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                    address.getLatitude(), address.getLongitude()), 16));
            hideInputKeyboard();
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(
                            address.getLatitude(), address.getLongitude()))
                   .title(address.getAddressLine(0)));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableLocation();
                } else {
                    //Permission not granted
                    finish();
                }
        }
    }

    @SuppressLint("MissingPermission")
    private void enableLocation(){
        mMap.setMyLocationEnabled(true);
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                userLocation = location;
                userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16));
            }
        });


    }

    private String getLocationName(Location location){
        String address ="";
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude()
                    , location.getLongitude(), 1);


            if(addressList != null && addressList.size()>0){
                Log.d("Address", addressList.get(0).toString());
                if(addressList.get(0).getCountryName() != null){
                    address +=  addressList.get(0).getCountryName();
                }
                if(addressList.get(0).getAddressLine(0) != null){
                    address +=  addressList.get(0).getAddressLine(0);
                }
                if(addressList.get(0).getSubAdminArea() != null){
                    address +=  addressList.get(0).getSubAdminArea();
                }
                //Toast.makeText(this, address,Toast.LENGTH_LONG).show();
            }else{
                Log.d("Address", "Unkown address");
                address += "Unknown address";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;

    }

    private void calculateDirections(Marker marker){
        com.google.maps.model.LatLng dest = new com.google.maps.model.LatLng(
                marker.getPosition().latitude, marker.getPosition().longitude);
        DirectionsApiRequest routes = new DirectionsApiRequest(geoApiContext);
        routes.alternatives(true);
        routes.origin(String.valueOf(userLatLng));
        routes.origin(
                new com.google.maps.model.LatLng(
                        userLatLng.latitude,
                        userLatLng.longitude
                )
        );
        routes.destination(String.valueOf(dest)).setCallback(
                new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                String routeDetails = "";
                routeDetails += result.routes[0].legs[0].duration;
                routeDetails += result.routes[0].legs[0].distance;
                routeDetails += result.geocodedWaypoints[0].toString();
                txt_route_info.setText(routeDetails);
                addRoutesToMap(result);
            }
            @Override
            public void onFailure(Throwable e) {
                Log.e("Routes", "onFailure: " + e.getMessage() );
                txt_route_info.setText("Unknown Address");
            }
        });
    }

    private void addRoutesToMap(DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(polylineDataList.size() > 0){
                    for(PolylineData polylineData: polylineDataList){
                        polylineData.getPolyline().remove();
                    }
                    polylineDataList.clear();
                    polylineDataList = new ArrayList<>();
                }

                double duration = 999999999;

                for(DirectionsRoute route: result.routes){
                    List<com.google.maps.model.LatLng> decodeRoute = PolylineEncoding
                            .decode(route.overviewPolyline.getEncodedPath());
                    List<LatLng> newDecodeRoute = new ArrayList<>();
                    int speedBumpNumber =0;
                    for(com.google.maps.model.LatLng latLng: decodeRoute){
                        newDecodeRoute.add(new LatLng(latLng.lat, latLng.lng));
                        if(speedBumpsList.contains(new LatLng(latLng.lat, latLng.lng))){
                            speedBumpNumber++;
                        }
                        if(speedBumpsListRounded.contains(new LatLng(roundNumber(latLng.lat),roundNumber(latLng.lng)))){
                            speedBumpNumber++;
                        }

                    }
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodeRoute));

                    polyline.setColor(ContextCompat.getColor(MapsActivity.this, R.color.darkGrey));
                    polyline.setClickable(true);
                    polylineDataList.add(new PolylineData(polyline, route.legs[0],speedBumpNumber));
                    btn_start_nav.setVisibility(View.VISIBLE);
                    btn_start_nav.setBackgroundColor(Color.BLUE);
                    btn_start_nav.setTextColor(Color.WHITE);

                    btn_start_nav.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startLocationUpdate();
                            mSensorManager.registerListener(sensorEventListenerAcc, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                            mSensorManager.registerListener(sensorEventListenerGyro, mGyroScope, SensorManager.SENSOR_DELAY_NORMAL);
                            btn_start_nav.setVisibility(View.INVISIBLE);
                            btn_stop_nav.setVisibility(View.VISIBLE);
                            btn_stop_nav.setBackgroundColor(Color.BLUE);
                            btn_stop_nav.setTextColor(Color.WHITE);
                        }
                    });

                    btn_stop_nav.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            stopLocationUpdate();
                            mSensorManager.unregisterListener(sensorEventListenerAcc);
                            mSensorManager.unregisterListener(sensorEventListenerGyro);
                            btn_stop_nav.setVisibility(View.INVISIBLE);
                        }
                    });

                    double mDuration = route.legs[0].duration.inSeconds;
                    if(mDuration < duration){
                        duration = mDuration;
                        onPolylineClick(polyline);
                    }


                }




            }



        });
    }

    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            updateGPS();
            return;
        }

    }

    private void stopLocationUpdate() {

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
    private void getSpeedBumpsLocation(){
        speedBumpsList.add(speedBump1);
        speedBumpsList.add(speedBump2);
        speedBumpsList.add(speedBump3);

        speedBumpsListRounded.add(speedBumpR1);
        speedBumpsListRounded.add(speedBumpR2);
        speedBumpsListRounded.add(speedBumpR3);
    }

    private void updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    updateLocationValues(location);
                }
            });

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
            }
        }
    }



    public SensorsData storeSensorsData() {

        SensorsData sensorsData = new SensorsData();

        sensorsData.setAccel_x(accel_x);
        sensorsData.setAccel_y(accel_y);
        sensorsData.setAccel_z(accel_z);
        sensorsData.setGyro_x(gyro_x);
        sensorsData.setGyro_y(gyro_y);
        sensorsData.setGyro_z(gyro_z);
        sensorsData.setLat(latitude);
        sensorsData.setLon(longitude);
        sensorsData.setAlt(altitude);
        return sensorsData;
    }

    public void updateLocationValues(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        altitude = location.getAltitude();

        txt_lat.setText("Latitude: " + String.valueOf(latitude));
        txt_lon.setText("Longitude: " + String.valueOf(longitude));
        txt_alt.setText("Altitude: " + String.valueOf(altitude));

    }





    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Location markerLocation = new Location("Marker");
        markerLocation.setLatitude(marker.getPosition().latitude);
        markerLocation.setLongitude(marker.getPosition().longitude);
        marker.setTitle(getLocationName(markerLocation));
        return false;
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        Location location = new Location("Long click marker");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        mMap.addMarker(new MarkerOptions().position(latLng).title(getLocationName(location)).snippet("Draw Routes?"));

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onPolylineClick(@NonNull Polyline polyline) {
        int routeNumber=0;
        for(PolylineData polylineData: polylineDataList){
            routeNumber ++;
            if(polyline.getId().equals(polylineData.getPolyline().getId())){
                polylineData.getPolyline().setColor(
                        ContextCompat.getColor(MapsActivity.this,R.color.blue));
                polylineData.getPolyline().setZIndex(1);
                txt_route_info.setText("Duration: "+ polylineData.getDirectionsLeg().duration +
                        " Distance: "+ polylineData.getDirectionsLeg().distance +
                        " Duration in Traffic: "+ polylineData.getDirectionsLeg()
                        .durationInTraffic + " Speed Bumps: "+polylineData.getSpeedBumpNumber()+
                        " Trip: "+routeNumber);
            }else{
                polylineData.getPolyline().setColor(
                        ContextCompat.getColor(MapsActivity.this,R.color.darkGrey));
                polylineData.getPolyline().setZIndex(0);
            }
        }
    }

    private void hideInputKeyboard(){
        View view = this.getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager)getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    private double roundNumber(double n){
        return (double)Math.round(n * 10000d) / 10000d;
    }


    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        if(markersPositionsList.size() == 1){
            markersList.get(0).remove();
            markersPositionsList.clear();
            markersList.clear();
            txt_route_info.setText("");
        }
        if(polylineDataList.size() > 0){
            for(PolylineData polylineData: polylineDataList){
                polylineData.getPolyline().remove();
            }
            polylineDataList.clear();
        }
        Location location = new Location("On map click marker");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);

        markersPositionsList.add(latLng);
        MarkerOptions targetMarkerOptions = new MarkerOptions()
                .position(markersPositionsList.get(0))
                .title(getLocationName(location)).snippet("Draw Routes?");
        targetMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(HUE_AZURE));
        Marker onMapClickMarker =  mMap.addMarker(targetMarkerOptions);
        markersList.add(onMapClickMarker);
    }

    @Override
    public void onInfoWindowClick(@NonNull  Marker marker) {
        Location location = new Location("Marker");
        location.setLatitude(marker.getPosition().latitude);
        location.setLongitude(marker.getPosition().longitude);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(marker.getSnippet())
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        calculateDirections(marker);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .create()
                .show();
    }
}