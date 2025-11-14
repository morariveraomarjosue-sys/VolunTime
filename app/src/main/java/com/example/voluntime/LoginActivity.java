package com.example.voluntime;

import android.content.Intent;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 🔹 Si el usuario ya está logueado, revisar perfil
        if (mAuth.getCurrentUser() != null) {
            checkUserProfile();
            return;
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoRegister = findViewById(R.id.btnGoRegister);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Bienvenido: " + mAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                            checkUserProfile();
                        } else {
                            Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void checkUserProfile() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // ✅ NUEVO: Verificar si el perfil está COMPLETO
                        String nombre = documentSnapshot.getString("nombre");
                        String apellido = documentSnapshot.getString("apellido");
                        String ciudad = documentSnapshot.getString("ciudad");

                        if (nombre != null && !nombre.isEmpty() &&
                                apellido != null && !apellido.isEmpty() &&
                                ciudad != null && !ciudad.isEmpty()) {

                            // ✅ PERFIL COMPLETO → MainActivity
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        } else {
                            // ❌ PERFIL INCOMPLETO → PerfilActivity
                            Toast.makeText(this, "Completa tu perfil", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, PerfilActivity.class));
                        }
                    } else {
                        // ❌ DOCUMENTO NO EXISTE → Crear y redirigir a PerfilActivity
                        String email = mAuth.getCurrentUser().getEmail();
                        HashMap<String, Object> usuario = new HashMap<>();
                        usuario.put("correo", email);
                        usuario.put("fechaRegistro", com.google.firebase.firestore.FieldValue.serverTimestamp());

                        db.collection("usuarios").document(uid)
                                .set(usuario)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Completa tu perfil", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, PerfilActivity.class));
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error creando perfil", Toast.LENGTH_SHORT).show();
                                });
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al revisar perfil", Toast.LENGTH_SHORT).show();
                });
    }
}