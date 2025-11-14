package com.example.voluntime;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ParticipanteAdapter extends RecyclerView.Adapter<ParticipanteAdapter.ViewHolder> {

    private List<Usuario> listaParticipantes;

    public ParticipanteAdapter(List<Usuario> listaParticipantes) {
        this.listaParticipantes = listaParticipantes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_participante, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Usuario participante = listaParticipantes.get(position);

        // Mostrar nombre completo
        String nombreCompleto = participante.getNombre() + " " + participante.getApellido();
        holder.tvNombre.setText(nombreCompleto);

        // Mostrar correo
        holder.tvCorreo.setText(participante.getCorreo());
    }

    @Override
    public int getItemCount() {
        return listaParticipantes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCorreo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreParticipante);
            tvCorreo = itemView.findViewById(R.id.tvCorreoParticipante);
        }
    }
}
