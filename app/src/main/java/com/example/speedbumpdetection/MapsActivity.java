package com.example.speedbumpdetection;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
        , GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener
        , GoogleMap.OnPolylineClickListener {

    TextView txt_route_info;
    EditText input_search;

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private static final int PERMISSION_FINE_LOCATION = 99;

    //Google API for GPS location services
    FusedLocationProviderClient fusedLocationProviderClient;

    //User Location
    private Location userLocation;
    private LatLng userLatLng;

    //Speed Bumps Markers
    ArrayList<LatLng> speedBumpsList = new ArrayList<>();
    LatLng speedBump1 = new LatLng(35.7296019, -0.5876281);
    LatLng speedBump2 = new LatLng(35.7302622, -0.5876606);
    LatLng speedBump3 = new LatLng(35.7291512, -0.5856882);
    MarkerOptions speedBumpMarkerOptions;

    Geocoder geocoder;

    //Directions
    GeoApiContext geoApiContext = null;

    ArrayList<PolylineData> polylineDataList = new ArrayList<>();








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
            speedBumpMarkerOptions = new MarkerOptions().position(speedBumpsList.get(i)).title("Speed Bump");
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

        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnPolylineClickListener(this);
        //mMap.setOnInfoWindowClickListener(this);

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
                //getLocationName(location);

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



        com.google.maps.model.LatLng dest = new com.google.maps.model.LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
        DirectionsApiRequest routes = new DirectionsApiRequest(geoApiContext);
        routes.alternatives(true);
        routes.origin(String.valueOf(userLatLng));
        routes.origin(
                new com.google.maps.model.LatLng(
                        userLatLng.latitude,
                        userLatLng.longitude
                )
        );
        routes.destination(String.valueOf(dest)).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                String routeDetails = "";
                Log.d("Routes", "onResult: routes: " + result.routes[0].toString());
                Log.d("Routes", "onResult: duration: " + result.routes[0].legs[0].duration);
                Log.d("Routes", "onResult: distance: " + result.routes[0].legs[0].distance);
                Log.d("Routes", "onResult: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());


                //routeDetails += result.routes[0].toString();
                routeDetails += result.routes[0].legs[0].duration;
                routeDetails += result.routes[0].legs[0].distance;
                //routeDetails += result.geocodedWaypoints[0].toString();

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

                    }
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodeRoute));

                    polyline.setColor(ContextCompat.getColor(MapsActivity.this, R.color.darkGrey));
                    polyline.setClickable(true);
                    polylineDataList.add(new PolylineData(polyline, route.legs[0],speedBumpNumber));

                    double mDuration = route.legs[0].duration.inSeconds;
                    if(mDuration < duration){
                        duration = mDuration;
                        onPolylineClick(polyline);
                    }

                }


            }


        });
    }

    private void getSpeedBumpsLocation(){
        speedBumpsList.add(speedBump1);
        speedBumpsList.add(speedBump2);
        speedBumpsList.add(speedBump3);
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        //Toast.makeText(this, marker.getTitle(),Toast.LENGTH_LONG).show();
        Location markerLocation = new Location("Marker");
        markerLocation.setLatitude(marker.getPosition().latitude);
        markerLocation.setLongitude(marker.getPosition().longitude);
        marker.setTitle(getLocationName(markerLocation));
        calculateDirections(marker);

        return false;
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        Location location = new Location("Long click marker");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        mMap.addMarker(new MarkerOptions().position(latLng).title(getLocationName(location)));

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
                LatLng destLocation = new LatLng(
                        polylineData.getDirectionsLeg().endLocation.lat,
                        polylineData.getDirectionsLeg().endLocation.lng
                );
                Marker destMarker = mMap.addMarker(new MarkerOptions()
                        .position(destLocation)
                        .title("trip: "+routeNumber)
                        .snippet("Duration: "+ polylineData.getDirectionsLeg().duration)

                );
                txt_route_info.setText("Duration: "+ polylineData.getDirectionsLeg().duration +
                        " Distance: "+ polylineData.getDirectionsLeg().distance +
                        " Duration in Traffic: "+ polylineData.getDirectionsLeg()
                        .durationInTraffic + " Speed Bumps: "+polylineData.getSpeedBumpNumber());

                destMarker.showInfoWindow();

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

}