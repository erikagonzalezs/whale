package com.example.taller3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.taller3.model.User;

import com.example.taller3.utils.References;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeActivity extends AppCompatActivity {
    User currentUser = new User();
    FirebaseDatabase database;
    DatabaseReference myRef;

    private int available = 1;
    private FirebaseAuth mAuth;

    Button btnAvailable;
    Button btnMyFriends;
    Button btnLogOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        btnAvailable = findViewById(R.id.btnAvailable);
        btnMyFriends = findViewById(R.id.btnMyFriends);
        btnLogOut = findViewById(R.id.btnLogOut);

        String uID = mAuth.getUid();
        myRef = FirebaseDatabase.getInstance().getReference();
        myRef.child(References.PATH_USERS).child(uID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(!task.isSuccessful()){
                    Log.i("Firebase", "Error getting data",task.getException());
                }else{
                    currentUser = task.getResult().getValue(User.class);
                }
            }
        });

        btnAvailable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!currentUser.getEstado()){
                    currentUser.setEstado(true);
                    myRef = database.getReference(References.PATH_USERS + mAuth.getUid() + "/estado");
                    myRef.setValue(currentUser.getEstado());
                    btnAvailable.setTextColor(Color.GREEN);
                }else if(currentUser.getEstado()){
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
    }
}