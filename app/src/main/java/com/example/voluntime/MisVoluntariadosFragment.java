package com.example.voluntime;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MisVoluntariadosFragment extends Fragment {

    private RecyclerView recyclerView;
    private VoluntariadoAdapter adapter;
    private List<Voluntariado> lista;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration listenerRegistration;
    private TextView tvEmpty;

    public MisVoluntariadosFragment() { /* constructor vacío */ }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mis_voluntariados, container, false);

        // Vistas
        recyclerView = view.findViewById(R.id.recyclerMisVoluntariados);
        tvEmpty = view.findViewById(R.id.tvEmptyMisVoluntariados);

        // Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // RecyclerView setup
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        lista = new ArrayList<>();
        adapter = new VoluntariadoAdapter(requireContext(), lista); // reutiliza tu adapter existente
        recyclerView.setAdapter(adapter);

        // Cargar los voluntariados creados por el usuario en tiempo real
        cargarMisVoluntariados();

        return view;
    }

    private void cargarMisVoluntariados() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        // Listener en tiempo real: voluntariados donde uid == current uid
        listenerRegistration = db.collection("voluntariados")
                .whereEqualTo("uid", uid)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(getContext(), "Error al cargar tus voluntariados", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    lista.clear();
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            Voluntariado v = doc.toObject(Voluntariado.class);
                            if (v != null) {
                                v.setDocumentId(doc.getId());
                                lista.add(v);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                    // Mostrar mensaje vacío si corresponde
                    tvEmpty.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }
}