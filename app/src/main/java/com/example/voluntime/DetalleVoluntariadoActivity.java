package com.example.voluntime;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetalleVoluntariadoActivity extends AppCompatActivity {

    private TextView tvTitulo, tvDescripcion, tvLugar, tvFecha, tvParticipantes;
    private Button btnUnirme;
    private RecyclerView rvParticipantes;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String documentId;
    private String titulo, descripcion, lugar, fecha, uidCreador;

    private ParticipanteAdapter participanteAdapter;
    private ArrayList<Usuario> listaParticipantes;
    private ProgressBar progressBarParticipantes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_voluntariado);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Referencias UI
        progressBarParticipantes = findViewById(R.id.progressBarParticipantes);
        tvTitulo = findViewById(R.id.tvDetalleTitulo);
        tvDescripcion = findViewById(R.id.tvDetalleDescripcion);
        tvLugar = findViewById(R.id.tvDetalleLugar);
        tvFecha = findViewById(R.id.tvDetalleFecha);
        tvParticipantes = findViewById(R.id.tvDetalleParticipantes);
        btnUnirme = findViewById(R.id.btnUnirme);
        rvParticipantes = findViewById(R.id.rvParticipantes);

        // RecyclerView
        rvParticipantes.setLayoutManager(new LinearLayoutManager(this));
        listaParticipantes = new ArrayList<>();
        participanteAdapter = new ParticipanteAdapter(listaParticipantes);
        rvParticipantes.setAdapter(participanteAdapter);

        // Obtener datos pasados desde el adapter
        if (getIntent() != null) {
            documentId = getIntent().getStringExtra("documentId");
            titulo = getIntent().getStringExtra("titulo");
            descripcion = getIntent().getStringExtra("descripcion");
            lugar = getIntent().getStringExtra("lugar");
            fecha = getIntent().getStringExtra("fecha");
            uidCreador = getIntent().getStringExtra("uid");
        }

        // Asignar datos a la vista
        tvTitulo.setText(titulo);
        tvDescripcion.setText(descripcion);
        tvLugar.setText("📍 " + lugar);
        tvFecha.setText("📅 " + fecha);

        // 🔹 Abrir Google Maps al tocar lugar
        tvLugar.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(lugar));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Toast.makeText(this, "No tienes Google Maps instalado", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ NUEVO: Verificar estado inicial del botón Unirme
        verificarEstadoUnirme();

        // ✅ CORREGIDO: Acción "Unirme" (actualiza numParticipantes)
        btnUnirme.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "Debes iniciar sesión para continuar", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentUid = mAuth.getCurrentUser().getUid();
            DocumentReference voluntariadoRef = db.collection("voluntariados").document(documentId);

            // Obtener el estado actual
            voluntariadoRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<String> participantes = (List<String>) documentSnapshot.get("participantes");
                    boolean yaUnido = participantes != null && participantes.contains(currentUid);

                    if (yaUnido) {
                        // ✅ CORREGIDO: Salir del voluntariado (actualiza ambos campos)
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("participantes", FieldValue.arrayRemove(currentUid));
                        updates.put("numParticipantes", FieldValue.increment(-1));

                        voluntariadoRef.update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Has salido del voluntariado ❌", Toast.LENGTH_SHORT).show();
                                    btnUnirme.setText("Unirme");
                                    contarParticipantes();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                    } else {
                        // ✅ CORREGIDO: Unirse al voluntariado (actualiza ambos campos)
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("participantes", FieldValue.arrayUnion(currentUid));
                        updates.put("numParticipantes", FieldValue.increment(1));

                        voluntariadoRef.update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Te uniste al voluntariado 🎉", Toast.LENGTH_SHORT).show();
                                    btnUnirme.setText("Salir");
                                    contarParticipantes();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                    }
                }
            });
        });

        // 🔹 Mostrar número de participantes
        contarParticipantes();
    }

    // ✅ NUEVO: Método para verificar estado del botón Unirme
    private void verificarEstadoUnirme() {
        if (mAuth.getCurrentUser() != null) {
            String currentUid = mAuth.getCurrentUser().getUid();
            db.collection("voluntariados").document(documentId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            List<String> participantes = (List<String>) documentSnapshot.get("participantes");
                            boolean yaUnido = participantes != null && participantes.contains(currentUid);

                            if (yaUnido) {
                                btnUnirme.setText("Salir");
                            } else {
                                btnUnirme.setText("Unirme");
                            }
                        }
                    });
        }
    }

    // ✅ ACTUALIZADO: Método para contar participantes (estructura array)
    private void contarParticipantes() {
        db.collection("voluntariados")
                .document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // ✅ Obtener participantes del array (no de subcolección)
                        List<String> participantes = (List<String>) documentSnapshot.get("participantes");
                        int count = participantes != null ? participantes.size() : 0;

                        tvParticipantes.setText("👥 Participantes: " + count);

                        // Si es creador y hay participantes, mostrar lista
                        if (mAuth.getCurrentUser() != null &&
                                mAuth.getCurrentUser().getUid().equals(uidCreador) &&
                                participantes != null && !participantes.isEmpty()) {

                            cargarListaParticipantes(participantes);
                        } else {
                            rvParticipantes.setVisibility(View.GONE);
                            progressBarParticipantes.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al contar participantes", Toast.LENGTH_SHORT).show();
                });
    }

    // ✅ NUEVO: Método para cargar lista de participantes
    private void cargarListaParticipantes(List<String> participantesUids) {
        listaParticipantes.clear();

        if (participantesUids == null || participantesUids.isEmpty()) {
            rvParticipantes.setVisibility(View.GONE);
            progressBarParticipantes.setVisibility(View.GONE);
            return;
        }

        // Mostrar ProgressBar
        progressBarParticipantes.setVisibility(View.VISIBLE);
        rvParticipantes.setVisibility(View.GONE);

        // Contador para consultas
        final int[] consultasCompletadas = {0};
        final int totalConsultas = participantesUids.size();

        for (String uid : participantesUids) {
            db.collection("usuarios").document(uid)
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        if (userDoc.exists()) {
                            Usuario usuario = userDoc.toObject(Usuario.class);
                            if (usuario != null) {
                                listaParticipantes.add(usuario);
                            }
                        }

                        consultasCompletadas[0]++;

                        if (consultasCompletadas[0] == totalConsultas) {
                            participanteAdapter.notifyDataSetChanged();
                            progressBarParticipantes.setVisibility(View.GONE);
                            rvParticipantes.setVisibility(View.VISIBLE);

                            // Esto nos dirá cuántos se cargaron
                            Toast.makeText(this, "Se cargaron: " + listaParticipantes.size() + " de " + totalConsultas, Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        consultasCompletadas[0]++;
                        if (consultasCompletadas[0] == totalConsultas) {
                            participanteAdapter.notifyDataSetChanged();
                            progressBarParticipantes.setVisibility(View.GONE);
                            rvParticipantes.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }
}