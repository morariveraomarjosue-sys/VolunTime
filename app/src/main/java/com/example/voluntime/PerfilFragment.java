package com.example.voluntime;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PerfilFragment extends Fragment {

    private ImageView imgPerfil;
    private EditText etNombre, etApellido, etCiudad;
    private TextView tvCorreo, tvFechaRegistro;
    private Button btnEditar, btnGuardar, btnCerrarSesion;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference userRef;

    private boolean editando = false;

    public PerfilFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        // Vincular vistas
        imgPerfil = view.findViewById(R.id.imgPerfil);
        etNombre = view.findViewById(R.id.etNombre);
        etApellido = view.findViewById(R.id.etApellido);
        etCiudad = view.findViewById(R.id.etCiudad);
        tvCorreo = view.findViewById(R.id.tvCorreo);
        tvFechaRegistro = view.findViewById(R.id.tvFechaRegistro);
        btnEditar = view.findViewById(R.id.btnEditar);
        btnGuardar = view.findViewById(R.id.btnGuardar);
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            userRef = db.collection("usuarios").document(user.getUid());
            cargarDatosUsuario();
        } else {
            Toast.makeText(getContext(), "No hay sesión activa. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show();

            // Redirigir al login
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
            return view;
        }

        btnEditar.setOnClickListener(v -> habilitarEdicion(true));

        btnGuardar.setOnClickListener(v -> guardarCambios());

        // ✅ NUEVO: Listener para cerrar sesión
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());

        return view;
    }

    private void cargarDatosUsuario() {
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String nombre = documentSnapshot.getString("nombre");
                String apellido = documentSnapshot.getString("apellido");
                String ciudad = documentSnapshot.getString("ciudad");
                String correo = documentSnapshot.getString("correo");

                // Manejar fecha de registro
                String fechaRegistro = "";
                if (documentSnapshot.get("fechaRegistro") instanceof Timestamp) {
                    Timestamp timestamp = documentSnapshot.getTimestamp("fechaRegistro");
                    if (timestamp != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        fechaRegistro = sdf.format(timestamp.toDate());
                    }
                } else {
                    fechaRegistro = documentSnapshot.getString("fechaRegistro");
                }

                etNombre.setText(nombre);
                etApellido.setText(apellido);
                etCiudad.setText(ciudad);
                tvCorreo.setText("Correo: " + (correo != null ? correo : "No disponible"));
                tvFechaRegistro.setText("Fecha de registro: " + (fechaRegistro != null ? fechaRegistro : "No disponible"));
            } else {
                // Si el documento no existe, crearlo vacío con el correo
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    Map<String, Object> nuevoUsuario = new HashMap<>();
                    nuevoUsuario.put("correo", user.getEmail());
                    nuevoUsuario.put("fechaRegistro", Timestamp.now());

                    userRef.set(nuevoUsuario).addOnSuccessListener(aVoid ->
                            Toast.makeText(getContext(), "Perfil inicial creado", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Error al cargar perfil", Toast.LENGTH_SHORT).show()
        );
    }

    private void habilitarEdicion(boolean habilitar) {
        etNombre.setEnabled(habilitar);
        etApellido.setEnabled(habilitar);
        etCiudad.setEnabled(habilitar);
        btnGuardar.setEnabled(habilitar);
        btnEditar.setEnabled(!habilitar);
        editando = habilitar;
    }

    private void guardarCambios() {
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String ciudad = etCiudad.getText().toString().trim();

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(apellido) || TextUtils.isEmpty(ciudad)) {
            Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", nombre);
        updates.put("apellido", apellido);
        updates.put("ciudad", ciudad);

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                    habilitarEdicion(false);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al actualizar perfil", Toast.LENGTH_SHORT).show()
                );
    }

    // ✅ NUEVO: Método para cerrar sesión
    private void cerrarSesion() {
        // Mostrar confirmación
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que quieres cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Cerrar sesión en Firebase
                    mAuth.signOut();

                    Toast.makeText(getContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show();

                    // Redirigir al Login
                    if (getActivity() != null) {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        getActivity().finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}