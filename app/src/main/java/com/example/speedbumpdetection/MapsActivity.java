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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.model.Document;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
        , GoogleMap.OnMarkerClickListener, GoogleMap.OnPolylineClickListener, GoogleMap.OnMapClickListener
        , GoogleMap.OnInfoWindowClickListener{

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

    //Firestore database
    FirebaseFirestore speedBumpsDB;

    //Speed Bumps Markers
    ArrayList<LatLng> speedBumpsList = new ArrayList<>();
    MarkerOptions speedBumpMarkerOptions;

    Geocoder geocoder;

    //Directions
    GeoApiContext geoApiContext = null;
    ArrayList<PolylineData> polylineDataList = new ArrayList<>();

    ArrayList<LatLng> markersPositionsList = new ArrayList<>();
    ArrayList<Marker> markersList = new ArrayList<>();

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Geo location search
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
        speedBumpsDB = FirebaseFirestore.getInstance();

        //Get and display the speed bumps locations
        getSpeedBumpsLocations();

        if (ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableLocation();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
            }
        }

        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnPolylineClickListener(this);
        mMap.setOnInfoWindowClickListener(this);

        if(geoApiContext == null){
            geoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_maps_key))
                    .build();
        }
    }

    //Get and display the speed bumps locations
    private void getSpeedBumpsLocations(){
        speedBumpsDB.collection("SpeedBumps").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()){
                            for(DocumentSnapshot d: queryDocumentSnapshots.getDocuments()){
                                SpeedBump sb = d.toObject(SpeedBump.class);
                                speedBumpsList.add(new LatLng(sb.getLat(), sb.getLon()));
                            }
                            //Add speed bumps locations to the map
                            for(int i=0; i<speedBumpsList.size();i++){
                                speedBumpMarkerOptions = new MarkerOptions().position(
                                        speedBumpsList.get(i)).title("Speed Bump");
                                speedBumpMarkerOptions.icon(BitmapDescriptorFactory
                                        .fromResource(R.drawable.speed_bump));
                                mMap.addMarker(speedBumpMarkerOptions);
                            }
                        }
                    }
                });
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
        //Request Google Direction API
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
                addRoutesToMap(result);
            }
            @Override
            public void onFailure(Throwable e) {
                txt_route_info.setText(e.getMessage());
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
                    for(com.google.maps.model.LatLng latLng: decodeRoute){
                        newDecodeRoute.add(new LatLng(latLng.lat, latLng.lng));
                    }
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodeRoute));
                    polyline.setColor(ContextCompat.getColor(MapsActivity.this, R.color.darkGrey));
                    polyline.setClickable(true);
                    polylineDataList.add(new PolylineData(polyline, route.legs[0]));
                    double mDuration = route.legs[0].duration.inSeconds;
                    if(mDuration < duration){
                        duration = mDuration;
                        onPolylineClick(polyline);
                    }
                }
            }
        });
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Location markerLocation = new Location("Marker");
        markerLocation.setLatitude(marker.getPosition().latitude);
        markerLocation.setLongitude(marker.getPosition().longitude);
        marker.setTitle(getLocationName(markerLocation));
        return false;
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
                        .durationInTraffic + " Trip: "+routeNumber);
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
        //Getting the location of the marker
        Location location = new Location("Marker");
        location.setLatitude(marker.getPosition().latitude);
        location.setLongitude(marker.getPosition().longitude);
        //Alert Dialog
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