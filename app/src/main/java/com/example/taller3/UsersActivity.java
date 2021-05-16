package com.example.taller3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;

import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.taller3.model.Modelo;
import com.example.taller3.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.taller3.utils.References;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import com.example.taller3.adapter.ListAdapter;
import com.google.firebase.storage.UploadTask;

import static com.example.taller3.utils.References.PATH_USERS;

public class UsersActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    //auth
    private FirebaseAuth mAuth;
    private StorageReference storage;

    //view
    ListView listUsers;
    private ArrayList<Modelo> mLista = new ArrayList<Modelo>();
    ListAdapter mAdapter;
    Modelo modelo;

    //Firebase database
    FirebaseDatabase dataBase;
    DatabaseReference myRef;
    FirebaseStorage strg_instancia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        //database
        dataBase = FirebaseDatabase.getInstance();
        //auth
        mAuth = FirebaseAuth.getInstance();
        //storage
        strg_instancia = FirebaseStorage.getInstance();
        storage = FirebaseStorage.getInstance().getReference();

        //inflate
        listUsers = findViewById(android.R.id.list);
        listUsers.setOnItemClickListener(this);

        mLista = new ArrayList<Modelo>();
        mAdapter = new ListAdapter(getApplicationContext(), R.layout.users_list, mLista);
        listUsers.setAdapter(mAdapter);


        subscribeToChange();
    }

    private void subscribeToChange() {
        //auth
        FirebaseUser currentUser = mAuth.getCurrentUser();
        myRef = dataBase.getReference(References.PATH_USERS);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mLista.clear();
                mAdapter.clear();
                mAdapter.notifyDataSetChanged();
                for (DataSnapshot single : snapshot.getChildren()) {
                    User user = single.getValue(User.class);
                    if (user.getEstado() && !single.getKey().equals(currentUser.getUid())) {
                        String link = (PATH_USERS + "profile_pictures/" + single.getKey() + ".jpg");
                        modelo = new Modelo(user.getNombre(), user.getApellido(), link, single.getKey());
                        mLista.add(modelo);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, InfoActivity.class);
        Bundle extras = new Bundle();
        extras.putString("nombre", mAdapter.getItem(position).getNombre());
        extras.putString("apellido", mAdapter.getItem(position).getApellido());
        extras.putString("Id",mAdapter.getItem(position).getId());
        intent.putExtras(extras);
        startActivity(intent);
    }
}