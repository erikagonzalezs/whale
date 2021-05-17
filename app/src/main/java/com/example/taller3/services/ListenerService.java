package com.example.taller3.services;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.taller3.HomeActivity;
import com.example.taller3.InfoActivity;
import com.example.taller3.R;
import com.example.taller3.UsersActivity;
import com.example.taller3.model.User;
import com.example.taller3.utils.References;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ListenerService extends JobIntentService {
    private static final int JOB_ID = 15;

    //auth
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    //database
    FirebaseDatabase database;
    DatabaseReference myRef;
    ValueEventListener vel;

    Map<String, Boolean> usuarios;
    Map<String, String> id_users;

    String nombre;
    String apellido;
    String key;

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, ListenerService.class, JOB_ID, intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(UsersActivity.TAG, "EN EL ON CREATE");
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        myRef = database.getReference(References.PATH_USERS);

        usuarios = new HashMap<>();
        id_users = new HashMap<>();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                    User u = singleSnapshot.getValue(User.class);
                    usuarios.put(u.getIdentificacion(), u.getEstado());
                    id_users.put(u.getIdentificacion(), singleSnapshot.getKey());
                    Log.i("USER", u.getNombre());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.i(UsersActivity.TAG, "Firebase listener service started");
        vel = myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<User> cambios = new ArrayList<>();
                for (DataSnapshot single : snapshot.getChildren()) {
                    User u = single.getValue(User.class);
                    Log.i("USER for", u.getNombre());
                    cambios.add(u);
                }
                for (int i = 0; i < cambios.size(); i++) {
                    if(cambios.get(i).getEstado() != usuarios.get(cambios.get(i).getIdentificacion())){
                        if (cambios.get(i).getEstado() == true) {
                            nombre = cambios.get(i).getNombre();
                            apellido = cambios.get(i).getApellido();
                            key = id_users.get(cambios.get(i).getIdentificacion());
                            buildAndShowNotification(nombre, apellido, key);
                        }
                        usuarios.put(cambios.get(i).getIdentificacion(), cambios.get(i).getEstado());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void buildAndShowNotification(String nombre, String apellido, String key) {

        Log.i(UsersActivity.TAG, "Firebase data has been modified");
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this, HomeActivity.CHANNEL_ID);
        nBuilder.setSmallIcon(R.drawable.whale_icon);
        nBuilder.setContentTitle("Whale");
        nBuilder.setContentText(nombre + " está disponible, entra para echar un vistazo!");
        nBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);

        //Accion asociada a la notificación Intent}

        Intent intent = new Intent(this, InfoActivity.class);
        Bundle extras = new Bundle();
        extras.putString("nombre", nombre);
        extras.putString("apellido", apellido);
        extras.putString("Id", this.key);
        intent.putExtras(extras);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        nBuilder.setContentIntent(pendingIntent);
        nBuilder.setAutoCancel(true);
        //Remueve la notificacion cuando se toca

        int notificationId = 001;
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        //notificationId es un entero unico definido para cada notificacion que se lanza
        notificationManager.notify(notificationId, nBuilder.build());

    }
}
