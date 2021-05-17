package com.example.taller3;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.taller3.model.LocationU;
import com.example.taller3.model.User;
import com.example.taller3.services.ListenerService;
import com.example.taller3.utils.LocationManager;
import com.example.taller3.utils.PermissionManager;
import com.example.taller3.utils.References;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {
    User currentUser = new User();
    FirebaseDatabase database;
    DatabaseReference myRef;

    private int available = 1;
    private FirebaseAuth mAuth;
    boolean change;
    Button btnAvailable;
    Button btnMyFriends;
    Button btnLogOut;
    LocationU[] locations;

    //maps
    private GoogleMap mMap;
    Geocoder geocoder;
    Marker currentposition;

    //gps
    final double RADIUS = 6371.01;
    //settings
    private static final int LOCATION_PERMISSION_ID = 10;
    private static final int SETTINGS_GPS = 10;
    private static final String LOCATION_NAME = Manifest.permission.ACCESS_FINE_LOCATION;

    //location
    private FusedLocationProviderClient locationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location userLocation;
    int actualLocCount;

    //notification
    public static String CHANNEL_ID = "NOTI_APP";

    private String justificacion = "Se requiere el GPS para acceder a la ubicación";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        btnAvailable = findViewById(R.id.btnAvailable);
        btnMyFriends = findViewById(R.id.btnMyFriends);
        btnLogOut = findViewById(R.id.btnLogOut);
        actualLocCount = 0;
        change = true;



        String uID = mAuth.getUid();
        myRef = FirebaseDatabase.getInstance().getReference();
        myRef.child(References.PATH_USERS).child(uID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.i("Firebase", "Error getting data", task.getException());
                } else {
                    currentUser = task.getResult().getValue(User.class);
                }
            }

        });

        locationRequest = LocationManager.createLocationRequest();
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    if (currentposition != null) {
                        currentposition.remove();
                    }
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    //Actualizacion ubicación
                    currentUser.setLatitud(latLng.latitude);
                    currentUser.setLongit(latLng.longitude);
                    myRef = database.getReference(References.PATH_USERS + mAuth.getUid() + "/latitud");
                    myRef.setValue(currentUser.getLatitud());
                    myRef = database.getReference(References.PATH_USERS + mAuth.getUid() + "/longit");
                    myRef.setValue(currentUser.getLongit());
                    //se coloca la ubicacion actual del usuario
                    currentposition = mMap.addMarker(new MarkerOptions().
                            position(latLng).title("Tu ubicación :" + LocationManager.geocoderSearch(latLng, geocoder)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    //se hace zoom solo la primera vez que se encuentra al usuario

                    if (userLocation != null) {
                        if (userLocation.getLatitude() != location.getLatitude() || userLocation.getLongitude() != location.getLongitude()) {
                            change = true;
                        }
                    }
                    userLocation = location;

                    if (change) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));

                        change = false;
                    }
                }
            }

        };
        PermissionManager.request_permission(this, LOCATION_NAME, justificacion, LOCATION_PERMISSION_ID);
        initView();
        geocoder = new Geocoder(this);


        btnAvailable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!currentUser.getEstado()) {
                    currentUser.setEstado(true);
                    myRef = database.getReference(References.PATH_USERS + mAuth.getUid() + "/estado");
                    myRef.setValue(currentUser.getEstado());
                    btnAvailable.setTextColor(Color.GREEN);
                } else if (currentUser.getEstado()) {
                    currentUser.setEstado(false);
                    myRef = database.getReference(References.PATH_USERS + mAuth.getUid() + "/estado");
                    myRef.setValue(currentUser.getEstado());
                    btnAvailable.setTextColor(Color.RED);
                }
            }
        });

        btnMyFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UsersActivity.class);
                startActivity(intent);
            }
        });

        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        createNotificationChannel();
    }


    //permisos gps
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_ID) {
            initView();
        }
    }

    //---------------------------------PERMISOS-------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_GPS) {
            startLocationUpdates();
        }
    }

    private void initView() {
        if (ContextCompat.checkSelfPermission(this, LOCATION_NAME) == PackageManager.PERMISSION_GRANTED) {
            checkSettingsLocation();
        }
    }

    private void checkSettingsLocation() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        //si el gps está on, inicia el uso del gps
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startLocationUpdates();
            }
        });

        //si el gps no está on, intente resolverlo
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes
                            .RESOLUTION_REQUIRED://GPS APAGADO PERO SE PUEDE ENCENDER PROGRAMATICAMENTE
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(HomeActivity.this, SETTINGS_GPS);
                        } catch (IntentSender.SendIntentException sendex) {
                        }
                        break;
                    case LocationSettingsStatusCodes
                            .SETTINGS_CHANGE_UNAVAILABLE:
                        Toast.makeText(HomeActivity.this, "El gps no funciona correctamente", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, LOCATION_NAME) == PackageManager.PERMISSION_GRANTED) {
            locationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentposition.getPosition(), 12));
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
        createNotificationChannel();
    }

    public void loadJson() {
        try {
            JSONObject json = new JSONObject(loadJSONFromAsset());
            JSONArray locationsJsonArray = json.getJSONArray("locationsArray");
            createArray(locationsJsonArray.length());
            ArrayAdapter<LocationU> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, locations);
            for (int i = 0; i < locationsJsonArray.length(); i++) {
                LocationU l = new LocationU("", 0, 0);
                JSONObject locationJson = locationsJsonArray.getJSONObject(i);
                l.setName(locationJson.getString("name"));
                l.setLat(locationJson.getDouble("latitude"));
                l.setLongit(locationJson.getDouble("longitude"));
                locations[i] = l;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void createArray(int tam) {
        locations = new LocationU[tam];
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = this.getAssets().open("ubicaciones.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        float number = 0;
        loadJson();
        for (int i = 0; i < locations.length; i++) {
            LatLng latLng2 = new LatLng(locations[i].getLat(), locations[i].getLongit());
            number += 60;
            Marker mark = mMap.addMarker(new MarkerOptions().position(latLng2).title(locations[i].getName()).icon(BitmapDescriptorFactory.defaultMarker(number)));

        }
        if (userLocation == null) {
            PermissionManager.request_permission(HomeActivity.this, LOCATION_NAME, justificacion, LOCATION_PERMISSION_ID);
            initView();
        }
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startListenerService();
    }

    private void startListenerService(){
        Intent intent = new Intent(HomeActivity.this, ListenerService.class);
        ListenerService.enqueueWork(HomeActivity.this, intent);
        Log.i(UsersActivity.TAG, "After invoking service");
    }

    //REUTILIZABLE NOTIFICACION
    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "channel";
            String description = "channel description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
