package com.example.fodie2.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.fodie2.R;
import com.example.fodie2.RestuItemSearch;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;


import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapsFragment extends Fragment {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100;
    private boolean locationPermissionGranted;
    LatLng current_location = null;
    GoogleMap map;
    LocationRequest request;
    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;
            getActivity().findViewById(R.id.autoComplete).setVisibility(View.VISIBLE);
            AutoCompleteTextView textView = getActivity().findViewById(R.id.autoComplete);

            textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String selection = parent.getItemAtPosition(position).toString();
                    Query query = FirebaseDatabase.getInstance().getReference().child("owners").orderByChild("name").equalTo(selection);
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Toast.makeText(getActivity(), snapshot.toString(), Toast.LENGTH_SHORT).show();
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                moveCameraTo(snapshot1.child("lat").getValue(String.class), snapshot1.child("lon").getValue(String.class));
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });
            getLocationPermission();

            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Toast.makeText(getActivity(), FirebaseDatabase.getInstance().getReference().child("owners").child("123456789").child("name").getKey().toString(), Toast.LENGTH_SHORT).show();

            search();
            googleMap.setMyLocationEnabled(true);
            checkGps();
            getLocationPermission();
            getcurrentLocation();
            addMarkers();

        }
    };

    private void addMarkers() {
        FirebaseDatabase Fdb = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = Fdb.getReference().child("owners");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    double lat = Double.valueOf(Objects.requireNonNull(snapshot1.child("lat").getValue()) + "d");
                    double lon = Double.valueOf(Objects.requireNonNull(snapshot1.child("lon").getValue()) + "d");
                    LatLng sydney = new LatLng(lat, lon);
                    map.addMarker(new MarkerOptions().position(sydney).title(snapshot1.child("name").getValue(String.class)).snippet( snapshot1.getKey().toString()));
                    map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(@NonNull Marker marker) {
                            Toast.makeText(getActivity(), marker.getSnippet(), Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(getActivity(), RestuItemSearch.class);
                            startActivity(i);
                            return true;
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void search() {
        FirebaseDatabase Fdb = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = Fdb.getReference().child("owners");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> arrayList = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        String address = snapshot1.child("address").getValue().toString();
                        String s = snapshot1.child("name").getValue().toString();
                        arrayList.add(s);
                    }
                    System.out.println(arrayList);
                    ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, arrayList);
                    AutoCompleteTextView textView = getActivity().findViewById(R.id.autoComplete);
                    textView.setAdapter(arrayAdapter);

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private Object lastKnownLocation;
    private Thread locationRequest;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        updateLocationUI();
    }


    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            checkGps();
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    private void checkGps() {
        request = request.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(5000);
        request.setFastestInterval(3000);
        LocationSettingsRequest.Builder builser = new LocationSettingsRequest.Builder().addLocationRequest(request).setAlwaysShow(true);
        Task<LocationSettingsResponse> locationSettingsRequestTask = LocationServices.getSettingsClient(getActivity()).checkLocationSettings(builser.build());

        locationSettingsRequestTask.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse request = task.getResult(ApiException.class);
                    Toast.makeText(getActivity(), "Gps Is Enabled", Toast.LENGTH_SHORT).show();
                } catch (ApiException e) {
                    if (e.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                        try {
                            resolvableApiException.startResolutionForResult(getActivity(), 101);
                        } catch (IntentSender.SendIntentException sendIntentException) {
                            sendIntentException.printStackTrace();
                        }
                    }
                    if (e.getStatusCode() == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                        Toast.makeText(getActivity(), "Setting prohibited  ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {

            if (resultCode == getActivity().RESULT_OK) {
                Toast.makeText(getActivity(), "GPS on", Toast.LENGTH_SHORT).show();

            } else if (resultCode == getActivity().RESULT_CANCELED) {
                Toast.makeText(getActivity(), "GPS unable to access", Toast.LENGTH_SHORT).show();

            }

        }
    }

    private void getcurrentLocation() {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Toast.makeText(getActivity(), "after", Toast.LENGTH_SHORT).show();
        LocationServices.getFusedLocationProviderClient(getActivity()).getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
//                Toast.makeText(getActivity(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                current_location = new LatLng(location.getLatitude(),location.getLongitude());
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(current_location, 0);
                map.moveCamera(update);
                Toast.makeText(getActivity(), "oi", Toast.LENGTH_SHORT).show();

            }
        });

    }
    private void moveCameraTo(String lat_string,String lon_string) {

        double lat = Double.valueOf(lat_string+"d");
        double lon = Double.valueOf(lon_string+"d");
        LatLng sydney = new LatLng(lat, lon);

        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(sydney, 20);
        map.moveCamera(update);
    }
}