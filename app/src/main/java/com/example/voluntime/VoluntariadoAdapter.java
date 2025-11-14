package com.example.voluntime;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoluntariadoAdapter extends RecyclerView.Adapter<VoluntariadoAdapter.ViewHolder> {

    private List<Voluntariado> lista;
    private Context context;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public VoluntariadoAdapter(Context context, List<Voluntariado> lista) {
        this.context = context;
        this.lista = lista;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_voluntariado, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Voluntariado v = lista.get(position);
        holder.tvTitulo.setText(v.getTitulo());
        holder.tvDescripcion.setText(v.getDescripcion());
        holder.tvLugarFecha.setText(v.getLugar() + " • " + v.getFecha());

        // ✅ NUEVO: Mostrar conteo de participantes
        holder.tvParticipantes.setText("👥 " + v.getNumParticipantes() + " participantes");

        String currentUid = (mAuth.getCurrentUser() != null) ? mAuth.getCurrentUser().getUid() : "";

        // ✅ NUEVO: Lógica del botón Unirme
        if (currentUid.equals(v.getUid())) {
            // Es el creador - ocultar botón Unirme
            holder.btnUnirme.setVisibility(View.GONE);
            holder.btnEditar.setVisibility(View.VISIBLE);
            holder.btnEliminar.setVisibility(View.VISIBLE);
        } else {
            // No es el creador - mostrar botón Unirme
            holder.btnUnirme.setVisibility(View.VISIBLE);
            holder.btnEditar.setVisibility(View.GONE);
            holder.btnEliminar.setVisibility(View.GONE);

            // Verificar si ya está unido
            boolean yaUnido = v.getParticipantes() != null &&
                    v.getParticipantes().contains(currentUid);

            if (yaUnido) {
                holder.btnUnirme.setText("Salir");
                holder.btnUnirme.setBackgroundTintList(
                        ContextCompat.getColorStateList(context, android.R.color.holo_red_dark));
            } else {
                holder.btnUnirme.setText("Unirme");
                holder.btnUnirme.setBackgroundTintList(
                        ContextCompat.getColorStateList(context, android.R.color.holo_green_dark));
            }

            // ✅ CORREGIDO: Listener para botón Unirme (actualiza numParticipantes en Firestore)
            holder.btnUnirme.setOnClickListener(view -> {
                if (mAuth.getCurrentUser() == null) {
                    Toast.makeText(context, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
                    return;
                }

                String uid = mAuth.getCurrentUser().getUid();
                DocumentReference voluntariadoRef = db.collection("voluntariados").document(v.getDocumentId());

                if (yaUnido) {
                    // ✅ CORREGIDO: Salir del voluntariado (actualiza ambos campos)
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("participantes", FieldValue.arrayRemove(uid));
                    updates.put("numParticipantes", FieldValue.increment(-1));

                    voluntariadoRef.update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Saliste del voluntariado", Toast.LENGTH_SHORT).show();
                                // Actualizar localmente
                                v.getParticipantes().remove(uid);
                                v.setNumParticipantes(v.getNumParticipantes() - 1);
                                notifyItemChanged(position);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(context, "Error al salir", Toast.LENGTH_SHORT).show());
                } else {
                    // ✅ CORREGIDO: Unirse al voluntariado (actualiza ambos campos)
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("participantes", FieldValue.arrayUnion(uid));
                    updates.put("numParticipantes", FieldValue.increment(1));

                    voluntariadoRef.update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Te uniste al voluntariado!", Toast.LENGTH_SHORT).show();
                                // Actualizar localmente
                                if (v.getParticipantes() == null) {
                                    v.setParticipantes(new java.util.ArrayList<>());
                                }
                                v.getParticipantes().add(uid);
                                v.setNumParticipantes(v.getNumParticipantes() + 1);
                                notifyItemChanged(position);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(context, "Error al unirse", Toast.LENGTH_SHORT).show());
                }
            });
        }

        // Listeners existentes para editar/eliminar...
        holder.btnEditar.setOnClickListener(view -> {
            Intent intent = new Intent(context, AddVoluntariadoActivity.class);
            intent.putExtra("documentId", v.getDocumentId());
            intent.putExtra("titulo", v.getTitulo());
            intent.putExtra("descripcion", v.getDescripcion());
            intent.putExtra("lugar", v.getLugar());
            intent.putExtra("fecha", v.getFecha());
            context.startActivity(intent);
        });

        holder.btnEliminar.setOnClickListener(view -> {
            new AlertDialog.Builder(context)
                    .setTitle("Eliminar voluntariado")
                    .setMessage("¿Estás seguro?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        db.collection("voluntariados").document(v.getDocumentId())
                                .delete()
                                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Eliminado", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        // Click en item → detalle
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, DetalleVoluntariadoActivity.class);
            intent.putExtra("documentId", v.getDocumentId());
            intent.putExtra("titulo", v.getTitulo());
            intent.putExtra("descripcion", v.getDescripcion());
            intent.putExtra("lugar", v.getLugar());
            intent.putExtra("fecha", v.getFecha());
            intent.putExtra("uid", v.getUid());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return lista.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvDescripcion, tvLugarFecha, tvParticipantes;
        Button btnEditar, btnEliminar, btnUnirme;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvLugarFecha = itemView.findViewById(R.id.tvLugarFecha);
            tvParticipantes = itemView.findViewById(R.id.tvParticipantes);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
            btnUnirme = itemView.findViewById(R.id.btnUnirme);
        }
    }
}