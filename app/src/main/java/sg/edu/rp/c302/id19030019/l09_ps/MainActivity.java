package sg.edu.rp.c302.id19030019.l09_ps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileWriter;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    Button updateBtn, removeBtn;
    private GoogleMap map;

    Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String[] Loc = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(MainActivity.this, Loc, 0);
        //folder direction
        String folderLocation = getFilesDir().getAbsolutePath() + "/MyFolder";

        File folder = new File(folderLocation);
        if (!folder.exists()) {
            boolean result = folder.mkdir();
            if (result) {
                Log.d("File read/write", "Folder Created");
            }
            else {
                Toast.makeText(MainActivity.this, "Failed to create", Toast.LENGTH_SHORT).show();
            }
        }
        File locationLog = new File(folderLocation, "data.txt");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.LatLong);
        updateBtn = findViewById(R.id.updateButton);
        removeBtn = findViewById(R.id.removeButton);

        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //in m
        locationRequest.setInterval(30 * 1000);
        //in m
        locationRequest.setSmallestDisplacement(500);
        //call back
        LocationCallback locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult result) {
                Location location = result.getLastLocation();
                textView.setText(msgs(location));
                if (marker == null) {
                    MarkerOptions options = new MarkerOptions()
                            .position(coordinatesFrom(location))
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    marker = map.addMarker(options);
                    map.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(coordinatesFrom(location), 15));
                }
                else {
                    marker.setPosition(coordinatesFrom(location));
                    map.moveCamera(CameraUpdateFactory
                            .newLatLng(coordinatesFrom(location)));
                }
                try {
                    FileWriter writer = new FileWriter(locationLog, true);
                    writer.write(coordinatesForRecords(location));
                    writer.flush();
                    writer.close();
                }
                catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Fail to log location", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        };
        FragmentManager fm = getSupportFragmentManager();
        SupportMapFragment mapFrag = (SupportMapFragment) fm.findFragmentById(R.id.mapView);

        mapFrag.getMapAsync(googleMap -> {
            map = googleMap;
            //zoom in SG
            LatLng SG = new LatLng(1.3521, 103.8198);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(SG, 15));
            //zoom controls
            UiSettings uiSettings = map.getUiSettings();
            uiSettings.setZoomControlsEnabled(true);

        });
        updateBtn.setOnClickListener(view -> {
            if (checkPermission()) {
                client.requestLocationUpdates(locationRequest, locationCallback, null);
            }
            else {
                failedPermission();
            }
        });
        removeBtn.setOnClickListener(view -> {
            if (checkPermission()) {
                client.removeLocationUpdates(locationCallback);
            }
            else {
                failedPermission();
            }
        });
    }
    private boolean checkPermission(){
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED
                || permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED) {
            return true;
        }
        else {
            return false;
        }
    }
    private String msgs(Location object) {
        return "Latitude: " + object.getLatitude()
                + "\nLongitude: " + object.getLongitude();
    }
    private void failedPermission() {
        Toast.makeText(MainActivity.this, "Location not found", Toast.LENGTH_SHORT).show();
    }
    private String coordinatesForRecords(Location object) {
        return object.getLatitude() + ", " + object.getLongitude() + "\n";
    }
    private LatLng coordinatesFrom(Location object) {
        return new LatLng(object.getLatitude(), object.getLongitude());
    }
}