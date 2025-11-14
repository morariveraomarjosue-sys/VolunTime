package com.example.voluntime;

import java.util.ArrayList;
import java.util.List;

public class Voluntariado {
    private String titulo;
    private String descripcion;
    private String lugar;
    private String fecha;
    private String uid;
    private String documentId;
    private List<String> participantes;
    private int numParticipantes;

    public Voluntariado() {
        this.participantes = new ArrayList<>();
        this.numParticipantes = 0;
    }

    public Voluntariado(String titulo, String descripcion, String lugar, String fecha, String uid) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.lugar = lugar;
        this.fecha = fecha;
        this.uid = uid;
        this.participantes = new ArrayList<>();
        this.numParticipantes = 0;
    }

    // Getters y Setters
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public List<String> getParticipantes() { return participantes; }
    public void setParticipantes(List<String> participantes) {
        this.participantes = participantes;
        this.numParticipantes = participantes != null ? participantes.size() : 0;
    }

    public int getNumParticipantes() { return numParticipantes; }
    public void setNumParticipantes(int numParticipantes) { this.numParticipantes = numParticipantes; }
}