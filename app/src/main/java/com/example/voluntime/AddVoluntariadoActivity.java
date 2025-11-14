package com.example.voluntime;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AddVoluntariadoActivity extends AppCompatActivity {

    private EditText etTitulo, etDescripcion, etLugar, etFecha;
    private Button btnAgregar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String documentId = null; // Para edición

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_voluntariado);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔹 Inicializar FirebaseAuth y Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 🔹 Vincular vistas
        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        etLugar = findViewById(R.id.etLugar);
        etFecha = findViewById(R.id.etFecha);
        btnAgregar = findViewById(R.id.btnAgregar);

        // 🔹 Revisar si vienen extras (modo edición)
        if (getIntent() != null) {
            documentId = getIntent().getStringExtra("documentId");
            String titulo = getIntent().getStringExtra("titulo");
            String descripcion = getIntent().getStringExtra("descripcion");
            String lugar = getIntent().getStringExtra("lugar");
            String fecha = getIntent().getStringExtra("fecha");

            if (documentId != null) {
                etTitulo.setText(titulo);
                etDescripcion.setText(descripcion);
                etLugar.setText(lugar);
                etFecha.setText(fecha);
                btnAgregar.setText("Guardar cambios");
            }
        }

        // 🔹 Acción al agregar o actualizar voluntariado
        btnAgregar.setOnClickListener(v -> {
            String titulo = etTitulo.getText().toString().trim();
            String descripcion = etDescripcion.getText().toString().trim();
            String lugar = etLugar.getText().toString().trim();
            String fecha = etFecha.getText().toString().trim();

            if (titulo.isEmpty() || descripcion.isEmpty() || lugar.isEmpty() || fecha.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            String uid = (user != null) ? user.getUid() : "anon";

            // ✅ ACTUALIZADO: Crear objeto con participantes
            Voluntariado vObj = new Voluntariado(titulo, descripcion, lugar, fecha, uid);

            if (documentId != null) {
                // Modo edición - mantener participantes existentes
                db.collection("voluntariados").document(documentId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                List<String> participantesExistentes = (List<String>) documentSnapshot.get("participantes");
                                if (participantesExistentes != null) {
                                    vObj.setParticipantes(participantesExistentes);
                                }

                                db.collection("voluntariados").document(documentId)
                                        .set(vObj)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Voluntariado actualizado", Toast.LENGTH_SHORT).show();
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        });
            } else {
                // Modo creación - participantes vacíos
                db.collection("voluntariados")
                        .add(vObj)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "Voluntariado agregado", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error al agregar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }
}