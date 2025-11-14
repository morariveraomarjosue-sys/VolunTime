package com.example.voluntime;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class PerfilActivity extends AppCompatActivity {

    private EditText etNombre, etApellido, etCiudad;
    private Button btnGuardar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etCiudad = findViewById(R.id.etCiudad);
        btnGuardar = findViewById(R.id.btnGuardar);

        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String apellido = etApellido.getText().toString().trim();
            String ciudad = etCiudad.getText().toString().trim();

            if(nombre.isEmpty() || apellido.isEmpty()){
                Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            guardarPerfil(nombre, apellido, ciudad);
        });
    }

    private void guardarPerfil(String nombre, String apellido, String ciudad){
        String uid = mAuth.getCurrentUser().getUid();
        String correo = mAuth.getCurrentUser().getEmail();

        HashMap<String, Object> usuario = new HashMap<>();
        usuario.put("nombre", nombre);
        usuario.put("apellido", apellido);
        usuario.put("correo", correo);
        usuario.put("ciudad", ciudad);
        usuario.put("fechaRegistro", FieldValue.serverTimestamp());

        db.collection("usuarios").document(uid)
                .set(usuario)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Perfil guardado", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(PerfilActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar perfil", Toast.LENGTH_SHORT).show());
    }
}