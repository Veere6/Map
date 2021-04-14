package com.example.mappoject;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {
    private GoogleMap mMap;
    EditText editText2, editText1;
    CardView cardView;
    TextView dis,dure;
    ImageView search;
    String location;
    private Polyline currentPolyline;
    MarkerOptions place1, place2;
    TextView get_direnction;
    TextView distence, time;
    String done="";
    FusedLocationProviderClient fusedLocationProviderClient;
    double pick_latitude, pick_longitute;
    double des_latitude, des_longitute;
    double distance;
    BottomSheetDialog bottomSheetDialog;
    double total_time;
    String current_l;
    FloatingActionButton floatingActionButton;
    LatLng start, end;
    View view;
    int zoomLevel =12;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText2 = findViewById(R.id.edittext2);
        editText1 = findViewById(R.id.edittext1);
        cardView=findViewById(R.id.card);
        dis=findViewById(R.id.dis);
        dure=findViewById(R.id.dure);
        floatingActionButton=findViewById(R.id.floating);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMapReady(mMap);
            }
        });

        getlocation();
        bottomSheetDialog=new BottomSheetDialog(this,R.style.Theme_AppCompat_Dialog_Alert);
        view = getLayoutInflater().inflate(R.layout.bottomsheet, null);
        bottomSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        bottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        bottomSheetDialog.setContentView(view);

        get_direnction = view.findViewById(R.id.getdirenction);
        distence = view.findViewById(R.id.distance);
        time = view.findViewById(R.id.time);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Places.initialize(MainActivity.this, "AIzaSyA38kNRqdVLWKnGYIjRfjp2Be0oJ4H0Ef4");
        editText2.setFocusable(false);
        editText1.setFocusable(false);
        editText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(MainActivity.this);
                startActivityForResult(intent, 100);
            }
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Handler handler = new Handler();


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                place1 = new MarkerOptions()
                        .position(new LatLng(pick_latitude, pick_longitute))
                        .title("Start");
                start = new LatLng(pick_latitude, pick_longitute);
                end = new LatLng(des_latitude, des_longitute);
                CameraUpdate zoom = CameraUpdateFactory.zoomTo(14);
                float zoomLevel = 16.0f; //This goes up to 21
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, zoomLevel));
                mMap.animateCamera(zoom);
                mMap.addMarker(place1);
                editText1.setText(current_l);
            }
        }, 1000);

        get_direnction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = getUrl(place1.getPosition(), place2.getPosition(), "driving");
                new FetchURL(MainActivity.this).execute(url, "driving");
                start = new LatLng(pick_latitude, pick_longitute);

                mMap.moveCamera(CameraUpdateFactory.newLatLng(start));
                CameraUpdate zoom = CameraUpdateFactory.zoomTo(zoomLevel);
                mMap.animateCamera(zoom);
                bottomSheetDialog.dismiss();
                cardView.setVisibility(View.VISIBLE);

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
//            address_edit.setText(place.getAddress());
            location = String.format("%s", place.getName());
            editText2.setText(location);
            if (!done.equals("")){
                mMap.clear();
                onMapReady(mMap);
                done="";
            }
            des_latitude = place.getLatLng().latitude;
            des_longitute = place.getLatLng().longitude;
            get_direnction.setVisibility(View.VISIBLE);
            bottomSheetDialog.show();
            endpoint();
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void endpoint() {
        place2 = new MarkerOptions().position(new LatLng(des_latitude, des_longitute)).title("End");
        start = new LatLng(pick_latitude, pick_longitute);
        end = new LatLng(des_latitude, des_longitute);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(end));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
        mMap.animateCamera(zoom);
        mMap.addMarker(place2);
        distance = SphericalUtil.computeDistanceBetween(start, end);
        done="1";
//        Toast.makeText(this, ""+distance, Toast.LENGTH_SHORT).show();
        double km=distance/1000;
        double total_km = Double.parseDouble(new DecimalFormat("##.##").format(km));
        distence.setText("("+total_km+"km)");
        dis.setText("Distance :"+total_km+"km");
        total_time = distance / 10;
        double minute=total_time/60;
        double total_distance = Double.parseDouble(new DecimalFormat("##.##").format(minute));
//        Toast.makeText(this, total_km + "", Toast.LENGTH_SHORT).show();
        if (total_distance>60){
            double hour=total_distance/60;
            double total_hour = Double.parseDouble(new DecimalFormat("##.##").format(hour));
            time.setText(total_hour+" Hour");
            dure.setText("Diretion :"+total_hour+ "Hour");
        }else{
            time.setText(total_distance+" Min");
            dure.setText("Diretion :"+total_distance+ "Min");
        }

    }

    private void getlocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    if (location != null) {

                        try {
                            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            pick_latitude = location.getLatitude();
                            pick_longitute = location.getLongitude();
                            current_l = addresses.get(0).getAddressLine(0);
//                            total_time=location.getSpeedAccuracyMetersPerSecond();

//                            Toast.makeText(MainActivity.this, current_l, Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 44);
        }
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);

    }


    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=AIzaSyA38kNRqdVLWKnGYIjRfjp2Be0oJ4H0Ef4";
        return url;
    }

}