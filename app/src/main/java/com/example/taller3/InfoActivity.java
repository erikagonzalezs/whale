package com.example.taller3;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taller3.model.User;
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
import com.google.android.gms.maps.internal.ICameraUpdateFactoryDelegate;
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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.ref.Reference;

public class InfoActivity extends FragmentActivity implements OnMapReadyCallback {

    //auth
    private FirebaseAuth mAuth;
    private StorageReference storage;

    private GoogleMap mMap;
    Geocoder geocoder;
    TextView nombre;
    TextView apellido;
    TextView distancia;

    Marker currentposition;
    Marker friendMark;
    String id;
    User friendU;
    User currentUser;
    int friendcount;
    //Firebase database
    FirebaseDatabase dataBase;
    DatabaseReference myRef;
    DatabaseReference friendref;
    FirebaseStorage strg_instancia;
    //--------------------------------
    private static final String LOCATION_NAME = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int LOCATION_PERMISSION_ID = 10;
    final double RADIUS = 6371.01;
    private static final int SETTINGS_GPS = 10;
    //------------------------location------------------------
    private FusedLocationProviderClient locationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location userLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        friendcount = 0;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        nombre = findViewById(R.id.tvInfoName);
        apellido = findViewById(R.id.tvInfoLastName);
        distancia = findViewById(R.id.tvDistance);
        friendU = new User();
        currentUser = new User();
        //--------
        //database
        dataBase = FirebaseDatabase.getInstance();
        //auth
        mAuth = FirebaseAuth.getInstance();
        //storage
        strg_instancia = FirebaseStorage.getInstance();
        storage = FirebaseStorage.getInstance().getReference();
        friendref = FirebaseDatabase.getInstance().getReference();
        myRef = dataBase.getReference();
        //ubicación usuario principal
        locationRequest = LocationManager.createLocationRequest();
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        //-----------------------------
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        id = extras.getString("Id");
        nombre.setText(extras.getString("nombre"));
        apellido.setText(extras.getString("apellido"));
        Log.i("friendId", id);

        myRef.child(References.PATH_USERS).child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.i("Firebase", "Error getting data", task.getException());
                } else {
                    friendU = task.getResult().getValue(User.class);
                }
            }
        });
        Log.i(String.valueOf(friendU.getLatitud()), String.valueOf(friendU.getLongit()));
        myRef.child(References.PATH_USERS).child(mAuth.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if (!task.isSuccessful()) {
                    Log.i("Firebase", "Error getting data", task.getException());
                } else {
                    currentUser = task.getResult().getValue(User.class);
                }
            }
        });
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
                    currentUser.setLatitud(latLng.latitude);
                    currentUser.setLongit(latLng.longitude);
                    myRef = dataBase.getReference(References.PATH_USERS + mAuth.getUid() + "/latitud");
                    myRef.setValue(currentUser.getLatitud());
                    myRef = dataBase.getReference(References.PATH_USERS + mAuth.getUid() + "/longit");
                    myRef.setValue(currentUser.getLongit());
                    userLocation = location;
                    currentposition = mMap.addMarker(new MarkerOptions().
                            position(latLng).title("Tu ubicación :" + LocationManager.geocoderSearch(latLng, geocoder)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    if (friendU.getLatitud() != 0.0 && friendU.getLongit() != 0.0) {
                        double distanc = distance(friendU.getLatitud(), friendU.getLongit(), currentUser.getLatitud(), currentUser.getLongit());
                        distancia.setText(Double.toString(distanc) + " KM");
                    }
                }
            }
        };
        PermissionManager.request_permission(this, LOCATION_NAME, "se requiere acceder a su ubicación", LOCATION_PERMISSION_ID);
        initView();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        geocoder = new Geocoder(this);
        subscribeToChange();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_ID) {
            initView();
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
                            resolvable.startResolutionForResult(InfoActivity.this, SETTINGS_GPS);
                        } catch (IntentSender.SendIntentException sendex) {
                        }
                        break;
                    case LocationSettingsStatusCodes
                            .SETTINGS_CHANGE_UNAVAILABLE:
                        Toast.makeText(InfoActivity.this, "El gps no funciona correctamente", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, LOCATION_NAME) == PackageManager.PERMISSION_GRANTED) {
            locationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void subscribeToChange() {
        friendref = dataBase.getReference(References.PATH_USERS + id);
        friendref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User aux = snapshot.getValue(User.class);
                if (aux.getEstado()) {
                    if (friendMark != null) {
                        friendMark.remove();
                    }

                    LatLng latLng = new LatLng(aux.getLatitud(), aux.getLongit());
                    if (latLng.longitude != 0.0 && latLng.latitude != 0.0) {
                        friendMark = mMap.addMarker(new MarkerOptions().position(latLng).title("ubicación de " + friendU.getNombre() + " : " + LocationManager.geocoderSearch(latLng, geocoder)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom((latLng), 13));
                        double distanc = distance(latLng.latitude, latLng.longitude, currentUser.getLatitud(), currentUser.getLongit());
                        distancia.setText(Double.toString(distanc) + " KM");
                    }


                }
                else
                {

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public double distance(double lat1, double long1, double lat2, double long2) {
        double latDistance = Math.toRadians(lat1 - lat2);
        double ingDistance = Math.toRadians(long1 - long2);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(ingDistance / 2) * Math.sin(ingDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double result = RADIUS * c;

        return Math.round(result * 100.0) / 100.0;

    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
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

        // Add a marker in Sydney and move the camera
        LatLng friend = new LatLng(friendU.getLatitud(), friendU.getLongit());
        Log.i("latt", String.valueOf(friend.latitude));
        Log.i("long", String.valueOf(friend.longitude));
        if (friend.latitude != 0.0 && friend.longitude != 0.0) {
            friendMark = mMap.addMarker(new MarkerOptions().position(friend).title("ubicación de " + friendU.getNombre() + " : " + LocationManager.geocoderSearch(friend, geocoder)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(friend, 13));
        }

    }
}