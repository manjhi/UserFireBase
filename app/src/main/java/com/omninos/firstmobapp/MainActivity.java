package com.omninos.firstmobapp;

import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private Marker drivers;
    //To calculate distance of to gmap points
    double _radiusEarthMiles = 3959;
    double _radiusEarthKM = 6371;
    double _m2km = 1.60934;
    double _toRad = Math.PI / 180;
    private double _centralAngle;
    private double Lat = 0.0, Lan = 0.0;
    private int count = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapHome);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        if (CommonUtils.isNetworkConnected(MainActivity.this)) {
            final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            DatabaseReference ref = database.child("User");

            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    count = 0;
                    System.out.println("Main: " + dataSnapshot.getChildrenCount());
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.getKey().equalsIgnoreCase(getIntent().getStringExtra("UserId"))) {
                            SingleSingleCars(snapshot.child("Location").child("PreLat"), snapshot.child("Location").child("PreLng"), snapshot.child("Location").child("Lat"), snapshot.child("Location").child("Lng"), count);
                            count++;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            Toast.makeText(this, "Please Check Internet Connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void createMarker(DataSnapshot lat, DataSnapshot lng, int count) {
        map.addMarker(new MarkerOptions()
                .position(new LatLng(Double.parseDouble(lat.getValue().toString()), Double.parseDouble(lng.getValue().toString())))
                .anchor(0.5f, 0.5f)
                .title("" + count)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
    }

    private void SingleSingleCars(DataSnapshot preLat, DataSnapshot preLng, DataSnapshot lat, DataSnapshot lng, int count) {
        List<LatLng> latLngData = new ArrayList<>();
        LatLng source = new LatLng(Double.parseDouble(preLat.getValue().toString()), Double.parseDouble(preLng.getValue().toString()));
        LatLng destination = new LatLng(Double.parseDouble(lat.getValue().toString()), Double.parseDouble(lng.getValue().toString()));
        latLngData.add(source);
        latLngData.add(destination);

        if (HasMoved(Double.parseDouble(lat.getValue().toString()), Double.parseDouble(lng.getValue().toString()))) {
            animateCarOnMap(latLngData);
        } else {
            if (drivers != null) {
                drivers.remove();
            }

            double dLon = (Double.parseDouble(lat.getValue().toString()) - Double.parseDouble(lng.getValue().toString()));
            double y = Math.sin(dLon) * Math.cos(Double.parseDouble(lat.getValue().toString()));
            double x = Math.cos(Double.parseDouble(lat.getValue().toString())) * Math.sin(Double.parseDouble(lat.getValue().toString())) - Math.sin(Double.parseDouble(lat.getValue().toString())) * Math.cos(Double.parseDouble(lat.getValue().toString())) * Math.cos(dLon);
            double brng = Math.toDegrees((Math.atan2(y, x)));
            brng = (360 - ((brng + 360) % 360));
            drivers = map.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(lat.getValue().toString()), Double.parseDouble(lng.getValue().toString())))
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
            drivers.setRotation((float) brng);
        }
    }

    private void animateCarOnMap(final List<LatLng> latLngs) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : latLngs) {
            builder.include(latLng);
        }
//        LatLngBounds bounds = builder.build();
//        CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
//        map.animateCamera(mCameraUpdate);


        if (drivers != null) {
            drivers.remove();
        }

        drivers = map.addMarker(new MarkerOptions().position(latLngs.get(0))
                .flat(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

        drivers.setPosition(latLngs.get(0));

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(1000);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Float v = valueAnimator.getAnimatedFraction();
                double lng = v * latLngs.get(1).longitude + (1 - v)
                        * latLngs.get(0).longitude;
                double lat = v * latLngs.get(1).latitude + (1 - v)
                        * latLngs.get(0).latitude;
                LatLng newPos = new LatLng(lat, lng);
                drivers.setPosition(newPos);
                drivers.setAnchor(0.5f, 0.5f);
                drivers.setRotation(getBearing(latLngs.get(0), newPos));
                map.animateCamera(CameraUpdateFactory.newCameraPosition
                        (new CameraPosition.Builder().target(newPos)
                                .zoom(15.5f).build()));
            }
        });
        valueAnimator.start();
    }

    private float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;

    }


    public boolean HasMoved(double newLat, double newLan) {
        double significantDistance = 30;
        double currentDistance;

        currentDistance = DistanceMilesSEP(Lat, Lan, newLat, newLan);
        currentDistance = currentDistance * _m2km * 1000;

        if (currentDistance < significantDistance) {
            return false;
        } else {
            Lat = newLat;
            Lan = newLan;
            return true;
        }
    }

    public double DistanceMilesSEP(double Lat1, double Lon1, double Lat2, double Lon2) {
        try {
            double _radLat1 = Lat1 * _toRad;
            double _radLat2 = Lat2 * _toRad;
            double _dLat = (_radLat2 - _radLat1);
            double _dLon = (Lon2 - Lon1) * _toRad;

            double _a = (_dLon) * Math.cos((_radLat1 + _radLat2) / 2);

            // central angle, aka arc segment angular distance
            _centralAngle = Math.sqrt(_a * _a + _dLat * _dLat);

            // great-circle (orthodromic) distance on Earth between 2 points
        } catch (Exception e) {
            e.printStackTrace();
        }
        return _radiusEarthMiles * _centralAngle;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
//        map.setMyLocationEnabled(true);

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_data));

            if (!success) {
                Log.e("Data", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("Data", "Can't find style. Error: ", e);
        }

    }
}
