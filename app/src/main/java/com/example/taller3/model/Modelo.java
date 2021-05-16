package com.example.taller3.model;

import android.graphics.Bitmap;
import android.widget.ImageView;

public class Modelo {
    private String nombre;
    private String apellido;
    private ImageView imagen;
    private String url;
    private String Id;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Modelo() {
    }

    public Modelo(String nombre, String apellido, String url, String id) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.url = url;
        this.Id = id;
    }

    public Modelo(String nombre, String apellido, ImageView imagen) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.imagen = imagen;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public ImageView getImagen() {
        return imagen;
    }

    public void setImagen(ImageView imagen) {
        this.imagen = imagen;
    }
}
