package com.example.taller3.model;

public class User {
    String nombre;
    String apellido;
    String identificacion;
    boolean estado;
    double latitud;
    double longit;

    public User() {
        estado = true;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongit() {
        return longit;
    }

    public void setLongit(double longit) {
        this.longit = longit;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public boolean getEstado() {
        return estado;
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

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }
}
