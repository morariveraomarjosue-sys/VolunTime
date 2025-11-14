package com.example.voluntime;

public class Usuario {
    private String nombre;
    private String apellido;
    private String ciudad;
    private String correo;
    private String fotoUrl;
    private Object fechaRegistro; // usamos Object para mapear el Timestamp de Firestore

    // Constructor vacío requerido por Firestore
    public Usuario() {
    }

    public Usuario(String nombre, String apellido, String ciudad, String correo, String fotoUrl, Object fechaRegistro) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.ciudad = ciudad;
        this.correo = correo;
        this.fotoUrl = fotoUrl;
        this.fechaRegistro = fechaRegistro;
    }

    // Getters y Setters
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

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public Object getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Object fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}