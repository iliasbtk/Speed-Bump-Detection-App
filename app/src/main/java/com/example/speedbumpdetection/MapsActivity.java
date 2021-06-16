package com.example.speedbumpdetection;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.Task;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
        , GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private static final int PERMISSION_FINE_LOCATION = 99;

    //Google API for GPS location services
    FusedLocationProviderClient fusedLocationProviderClient;

    //User Location
    private Location userLocation;

    //Speed Bumps Markers
    ArrayList<LatLng> speedBumpsList = new ArrayList<>();
    LatLng speedBump1 = new LatLng(35.7296019, -0.5876281);
    LatLng speedBump2 = new LatLng(35.7302622, -0.5876606);
    MarkerOptions speedBumpMarkerOptions;

    Geocoder geocoder;





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
                LatLng latLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                //getLocationName(location);

            }
        });


    }

    private void getLocationName(Location location){

        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude()
                    , location.getLongitude(), 1);
            String address ="";

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
                Toast.makeText(this, address,Toast.LENGTH_LONG).show();
            }else{
                Log.d("Address", "Unkown address");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void getSpeedBumpsLocation(){
        speedBumpsList.add(speedBump1);
        speedBumpsList.add(speedBump2);
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        //Toast.makeText(this, marker.getTitle(),Toast.LENGTH_LONG).show();
        Location markerLocation = new Location("Marker");
        markerLocation.setLatitude(marker.getPosition().latitude);
        markerLocation.setLongitude(marker.getPosition().longitude);
        getLocationName(markerLocation);



        return false;
    }
}