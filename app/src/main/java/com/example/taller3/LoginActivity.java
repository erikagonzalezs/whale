package com.example.taller3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.taller3.utils.PermissionManager;

public class LoginActivity extends AppCompatActivity {

    //permisos
    private static final int MAPS_PERMISSION_ID = 3;

    private static final String MAPS_NAME = Manifest.permission.ACCESS_FINE_LOCATION;
    //auth
    private FirebaseAuth mAuth;
    public static final String TAG = "FB tag";

    EditText email;
    EditText password;
    Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        //inflate
        email =  findViewById(R.id.etEmail);
        password = findViewById(R.id.etPassword);
        login = findViewById(R.id.btnIniciarSesion);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               maps(v);
            }
        });
    }

    protected void maps(View view){
        PermissionManager.request_permission(
                this,
                MAPS_NAME,
                "Se necesita acceder a la localización para poder iniciar sesión",
                MAPS_PERMISSION_ID
        );
        if(PermissionManager.checkPermission(this, MAPS_NAME)){
            String correo = email.getText().toString();
            String contrasena = password.getText().toString();
            if(validar(correo, contrasena)){
                signIn(correo, contrasena);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUI(mAuth.getCurrentUser());
    }

    private void updateUI(FirebaseUser currentUser){
        if(currentUser != null){
            Intent intent = new Intent(getBaseContext(), HomeActivity.class);
            intent.putExtra("user", currentUser.getEmail());
            startActivity(intent);
        }else{
            email.setText("");
            password.setText("");
        }
    }

    private void signIn(String correo, String contrasena){
        mAuth.signInWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });

    }

    private Boolean validar(String email, String password){
        Pattern pattern = Pattern.compile("([a-z0-9]+(\\.?[a-z0-9])*)+@(([a-z]+)\\.([a-z]+))+");
        if(email != null && password != null){
            if(!email.isEmpty()&& !password.isEmpty()){
                Matcher mather = pattern.matcher(email);
                if(mather.find() && password.length()>5){
                    return true;
                }else{
                    if(!mather.find()) {
                        this.email.setError("Correo inválido");
                    }
                    if(password.length()<5) {
                        this.password.setError("La contraseña debe de tener más de 5 caracteres");
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == MAPS_PERMISSION_ID){
            if(PermissionManager.checkPermission(this, MAPS_NAME)){
                String correo = email.getText().toString();
                String contrasena = password.getText().toString();
                if(validar(correo, contrasena)){
                    signIn(correo, contrasena);
                }
            }
        }
    }
}