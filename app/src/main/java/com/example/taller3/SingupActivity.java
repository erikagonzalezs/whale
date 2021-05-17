package com.example.taller3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.taller3.utils.PermissionManager;
import com.example.taller3.model.User;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static com.example.taller3.utils.References.PATH_USERS;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SingupActivity extends AppCompatActivity {

    //permisos
    private static final int CAMERA_PERMISSION_ID = 1;
    private static final int IMAGE_PICKER_PERMISSION_ID = 2;
    private static final int MAPS_PERMISSION_ID = 3;

    private static final String CAMERA_NAME = Manifest.permission.CAMERA;
    private static final String IMAGE_PICKER_NAME = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String MAPS_NAME = Manifest.permission.ACCESS_FINE_LOCATION;

    //auth
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private StorageReference storage;
    DatabaseReference myRef;

    ImageView ivUserIcon;
    Button btnUploadProfileIcon;
    Button btnTakeProfileIcon;
    Button btnRegister;

    EditText nombre;
    EditText apellido;
    EditText correo;
    EditText contrasena;
    EditText identificacion;
    //location
    private FusedLocationProviderClient locationCliente;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location userLocation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singup);

        //auth
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference(PATH_USERS);
        storage = FirebaseStorage.getInstance().getReference();

        //inflate
        ivUserIcon = findViewById(R.id.ivUserIcon);
        btnUploadProfileIcon = findViewById(R.id.btnUploadProfileIcon);
        btnTakeProfileIcon = findViewById(R.id.btnTakeProfileIcon);
        btnRegister = findViewById(R.id.btnRegister);

        //location
        locationRequest = createLocationRequest();
        locationCliente = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                if(location!=null)
                {
                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                    userLocation = location;
                }
            }
        };
            PermissionManager.request_permission(this, MAPS_NAME,"Se requiere acceder a la ubicación",MAPS_PERMISSION_ID);
            initView();
        //-------
        nombre = findViewById(R.id.etName);
        apellido = findViewById(R.id.etApellido);
        correo = findViewById(R.id.etRegisterCorreo);
        contrasena = findViewById(R.id.etRegisterPassword);
        identificacion = findViewById(R.id.etIdentificacion);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maps(v);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();

    }

    private void initView() {
        if(ContextCompat.checkSelfPermission(this,MAPS_NAME)== PackageManager.PERMISSION_GRANTED)
        {
            checkSettingsLocation();
        }
    }
    private void checkSettingsLocation()
    {
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
                int statusCode=((ApiException)e).getStatusCode();
                switch (statusCode)
                {
                    case CommonStatusCodes
                            .RESOLUTION_REQUIRED://GPS APAGADO PERO SE PUEDE ENCENDER PROGRAMATICAMENTE
                        try{
                            ResolvableApiException resolvable =(ResolvableApiException)e;
                            resolvable.startResolutionForResult(SingupActivity.this,MAPS_PERMISSION_ID);
                        }catch (IntentSender.SendIntentException sendex)
                        {
                        }
                        break;
                    case LocationSettingsStatusCodes
                            .SETTINGS_CHANGE_UNAVAILABLE:
                        Toast.makeText(SingupActivity.this,"El gps no funciona correctamente",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    //----------------
    private void startLocationUpdates()
    {
        if(ContextCompat.checkSelfPermission(this,MAPS_NAME)==PackageManager.PERMISSION_GRANTED)
        {
            locationCliente.requestLocationUpdates(locationRequest,locationCallback,null);
        }
    }


    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    public void maps(View view){
        PermissionManager.request_permission(
                this,
                MAPS_NAME,
                "Se necesita acceder a la localización para poder registrarse",
                MAPS_PERMISSION_ID
        );
        if(PermissionManager.checkPermission(this, MAPS_NAME)){
            register();
        }
    }

    public void upload(View view) {
        PermissionManager.request_permission(
                this,
                IMAGE_PICKER_NAME,
                "Se necesita acceder al album de fotos",
                IMAGE_PICKER_PERMISSION_ID
        );
        if(PermissionManager.checkPermission(this, IMAGE_PICKER_NAME)){
            pick_image();
        }

    }

    public void camera(View view) {
        PermissionManager.request_permission(
                this,
                CAMERA_NAME,
                "Se necesita la cámara para capturar la foto",
                CAMERA_PERMISSION_ID
        );
        if(PermissionManager.checkPermission(this, CAMERA_NAME)){
            take_picture();
        }
    }
    private void pick_image(){
        Intent pick_imag_intent = new Intent(Intent.ACTION_PICK);
        pick_imag_intent.setType("image/*");
        startActivityForResult(pick_imag_intent, IMAGE_PICKER_PERMISSION_ID);
    }

    private void take_picture(){
        Intent take_picture_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(take_picture_intent, CAMERA_PERMISSION_ID);
    }

    private Boolean validar(String emails, String passwords, String names, String lastNames,
                            String identifications) {
        Boolean ret = true;
        Pattern pattern = Pattern.compile("([a-z0-9]+(\\.?[a-z0-9])*)+@(([a-z]+)\\.([a-z]+))+");
        Matcher mather = pattern.matcher(emails);
        //validación de datos
        if (names != null && lastNames != null && emails != null && passwords != null &&
                identifications != null) {
            if (names.isEmpty()) {
                nombre.setError("Información obligatoria");
                ret = false;
            }
            if (lastNames.isEmpty()) {
                apellido.setError("Información obligatoria");
                ret = false;
            }
            if (emails.isEmpty()) {
                correo.setError("Información obligatoria");
                ret = false;
            }
            if (passwords.isEmpty()) {
                contrasena.setError("Información obligatoria");
                ret = false;
            }
            if (identifications.isEmpty()) {
                identificacion.setError("Información obligatoria");
                ret = false;
            }

            if (ret) {
                if (mather.find() && passwords.length() > 5) {
                    return true;
                } else {
                    if (!mather.find()) {
                        this.correo.setError("Correo inválido");
                    }
                    if (passwords.length() < 5) {
                        this.contrasena.setError("La contraseña debe tener más de 5 caracteres");
                    }
                    ret = false;
                }
            }
        }else{
            ret = false;
        }
        return ret;
    }

    public void register(){
        String names = nombre.getText().toString();
        String lastNames = apellido.getText().toString();
        String emails = correo.getText().toString();
        String passwords = contrasena.getText().toString();
        String identifications = identificacion.getText().toString();

        if(validar(emails, passwords, names, lastNames, identifications)){
            mAuth.createUserWithEmailAndPassword(emails, passwords).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if(user != null){
                        User nUser = new User();
                        nUser.setNombre(names);
                        nUser.setApellido(lastNames);
                        nUser.setIdentificacion(identifications);
                        nUser.setLatitud(userLocation.getLatitude());
                        nUser.setLongit(userLocation.getLongitude());

                        if(ivUserIcon != null){
                            StorageReference profilePhoto = storage.child(PATH_USERS + "profile_pictures/" + user.getUid() + ".jpg");
                            ivUserIcon.setDrawingCacheEnabled(true);
                            ivUserIcon.buildDrawingCache();
                            Bitmap bitmap = ((BitmapDrawable) ivUserIcon.getDrawable()).getBitmap();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] data = baos.toByteArray();

                            UploadTask uploadTask = profilePhoto.putBytes(data);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(SingupActivity.this, "No se pudo crear el usuario", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Toast.makeText(SingupActivity.this, "El usuario fue creado correctamente", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        String key = user.getUid();
                        myRef = database.getReference(PATH_USERS + key);
                        myRef.setValue(nUser);
                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int request_code, int result_code, Intent data){
        super.onActivityResult(request_code, result_code, data);
        if(request_code == CAMERA_PERMISSION_ID && result_code == RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap bm = (Bitmap) extras.get("data");
            ivUserIcon.setImageBitmap(bm);
        }else if (request_code == IMAGE_PICKER_PERMISSION_ID && result_code == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                ivUserIcon.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        else if(request_code == MAPS_PERMISSION_ID)
        {
            startLocationUpdates();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_ID) {
            if (PermissionManager.checkPermission(this, CAMERA_NAME)) {
                take_picture();
            }
        }
        if (requestCode == IMAGE_PICKER_PERMISSION_ID) {
            if (PermissionManager.checkPermission(this, IMAGE_PICKER_NAME)) {
                pick_image();
            }
        }
        if(requestCode == MAPS_PERMISSION_ID){
            if(PermissionManager.checkPermission(this, MAPS_NAME)){
                initView();
                register();
            }
        }
    }
}