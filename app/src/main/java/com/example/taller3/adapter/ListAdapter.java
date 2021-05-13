package com.example.taller3.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.taller3.R;
import com.example.taller3.model.Modelo;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import static com.example.taller3.utils.References.PATH_USERS;

public class ListAdapter extends ArrayAdapter<Modelo> {

    private ArrayList<Modelo> mList;
    private Context mContext;
    private int resource_layout;
    private StorageReference storageReference;
    FirebaseStorage storageInstance;

    public ListAdapter(@NonNull Context context, int resource, ArrayList<Modelo> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.resource_layout = resource;
        this.mList = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(resource_layout, null);
        }

        storageInstance = FirebaseStorage.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        Modelo modelo = mList.get(position);
        //inflate
        ImageView imagen = view.findViewById(R.id.imageView);
        TextView nombre = view.findViewById(R.id.nameContact);
        TextView apellido = view.findViewById(R.id.lastName);

        storageReference = storageInstance.getReference(modelo.getUrl());
        storageReference.getBytes(8 * 1024 * 1024).addOnSuccessListener(v -> {
            Bitmap bm = BitmapFactory.decodeByteArray(v, 0 , v.length);
            imagen.setImageBitmap(bm);
        });

        nombre.setText(modelo.getNombre());
        apellido.setText(modelo.getApellido());

        return view;
    }
}
