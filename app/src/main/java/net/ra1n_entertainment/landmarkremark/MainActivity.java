package net.ra1n_entertainment.landmarkremark;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    // Elements
    private GoogleMap mMap;
    private EditText AddDialogTitle;
    private EditText AddDialogDescription;

    // Location permission status
    private Boolean locationPermissionGranted = false;

    // Permission request code
    private static final int REQUEST_CODE = 333;

    // Backendless API key & App id
    private static final String API_KEY = "612FA3D5-CBAA-DF3A-FFFE-A179685D4700";
    private static final String APP_ID = "D97C40DA-680B-C9E9-FF97-00705D3E9100";

    // Default zoom
    private static final int DEFAULT_ZOOM = 15;

    // Fused location client
    private FusedLocationProviderClient locationClient;

    // Last known location & default location
    private Location lastKnownLocation;
    private static final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);

    // Location manager stuff
    private LocationManager locationManager;
    public Criteria criteria;
    public String bestProvider;

    // Alert dialog
    Dialog dialog;

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init backendless
        Backendless.initApp(MainActivity.this, APP_ID, API_KEY);

        // Initialize fusedLocationProvider
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Init elements
        FloatingActionButton addNoteButton = findViewById(R.id.floatingActionButton);
        FloatingActionButton SearchButton = findViewById(R.id.mapSearchButton);

        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddDialog();
            }
        });

        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SearchNotes.class);
                startActivityForResult(intent, 999);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 999 && resultCode == RESULT_OK) {
            Map note = (Map) data.getSerializableExtra("note");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng((Double) note.get("latitude"),
                            (Double) note.get("longitude")), DEFAULT_ZOOM));
        }

    }

    public void showAddDialog() {
        dialog = new Dialog(this, R.style.AppTheme);
        dialog.setContentView(R.layout.alert);
        dialog.show();

        AddDialogTitle = dialog.findViewById(R.id.addDialogTitleInput);
        AddDialogDescription = dialog.findViewById(R.id.addDialogDescriptionInput);
        Button AddDialogButton = dialog.findViewById(R.id.addDialogButton);

        AddDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lastKnownLocation != null) {
                    saveNote();
                } else {
                    Toast.makeText(MainActivity.this, "Error, location has not been set", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

    public void addMarker(Map note) {
        mMap.addMarker(
                new MarkerOptions().position(
                        new LatLng((Double) note.get("latitude"), (Double) note.get("longitude")))
                        .title(note.get("title").toString())
                        .snippet("Posted by: " + note.get("username").toString() +
                                "\nDescription: " + note.get("description").toString() +
                                "\nLatitude: " + note.get("latitude").toString() +
                                "\nLongitude: " + note.get("longitude").toString()
                        ));
    }

    public void saveNote() {
        // Get values to store in the backend
        final String title = AddDialogTitle.getText().toString();
        final String description = AddDialogDescription.getText().toString();
        final Double latitude = lastKnownLocation.getLatitude();
        final Double longitude = lastKnownLocation.getLongitude();
        final String username =  getIntent().getExtras().getString("username");
        if ( title.isEmpty() || description.isEmpty() ) {
            Toast.makeText(this, "Fill in all fields", Toast.LENGTH_SHORT).show();
        } else {
            // Create a location HashMap with the above values
            final HashMap newNote = new HashMap();
            newNote.put("username", username);
            newNote.put("title", title);
            newNote.put("latitude", latitude);
            newNote.put("description", description);
            newNote.put("longitude", longitude);

            // Save object to backendless
            Backendless.Data.of("Notes").save(newNote, new AsyncCallback<Map>() {
                @Override
                public void handleResponse(Map response) {
                    addMarker(newNote);
                    dialog.hide();
                    Toast.makeText(MainActivity.this, "Saved note successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    dialog.hide();
                    Toast.makeText(MainActivity.this, fault.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * Load 25 notes from Backendless
     */
    public void loadNotes() {
        DataQueryBuilder query = DataQueryBuilder.create();
        query.setPageSize(25);
        Backendless.Data.of("Notes").find(new AsyncCallback<List<Map>>() {
            @Override
            public void handleResponse(List<Map> response) {
                for (Map note : response) {
                    addMarker(note);
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {

            }
        });
    }

    /**
     * Called when map is ready
     * @param googleMap googleMap object
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new NoteInfoAdapter(getLayoutInflater()));
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();
        loadNotes();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = locationClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            lastKnownLocation = task.getResult();

                            // This can happen if no previous app has recently requested the device's location
                            if (lastKnownLocation == null) {
                                locationManager = (LocationManager)  getSystemService(Context.LOCATION_SERVICE);
                                criteria = new Criteria();
                                bestProvider = locationManager.getBestProvider(criteria, true);
                            } else {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d("Err", "Current location is null. Using defaults.");
                            Log.e("Err", "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates((android.location.LocationListener) this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location.equals(lastKnownLocation)) {
            lastKnownLocation = location;

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(lastKnownLocation.getLatitude(),
                            lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
        }
    }
}