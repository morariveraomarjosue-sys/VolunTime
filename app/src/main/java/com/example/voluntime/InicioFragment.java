package com.example.voluntime;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class InicioFragment extends Fragment {

    private RecyclerView recyclerView;
    private VoluntariadoAdapter adapter;
    private List<Voluntariado> lista;
    private List<Voluntariado> listaCompleta;
    private FloatingActionButton fabAdd; // Ahora se obtiene del activity
    private FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;
    private ActivityResultLauncher<Intent> addVolLauncher;
    private SearchView searchView;
    private CheckBox cbMisVoluntariados;
    private FirebaseAuth mAuth;
    private Spinner spinnerOrden;

    public InicioFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        // 🔹 Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // 🔹 RecyclerView
        recyclerView = view.findViewById(R.id.recyclerVoluntariados);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        lista = new ArrayList<>();
        listaCompleta = new ArrayList<>();
        adapter = new VoluntariadoAdapter(getContext(), lista);
        recyclerView.setAdapter(adapter);

        // 🔹 FAB (del activity)
        fabAdd = requireActivity().findViewById(R.id.fabAddVoluntariado);
        fabAdd.show(); // asegurar que sea visible
        addVolLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> { /* se actualiza solo por snapshotListener */ }
        );
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddVoluntariadoActivity.class);
            addVolLauncher.launch(intent);
        });

        // 🔹 Barra de búsqueda
        searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { filtrar(query); return true; }
            @Override
            public boolean onQueryTextChange(String newText) { filtrar(newText); return true; }
        });

        // 🔹 CheckBox "Mis voluntariados"
        cbMisVoluntariados = view.findViewById(R.id.cbMisVoluntariados);
        cbMisVoluntariados.setOnCheckedChangeListener((buttonView, isChecked) ->
                filtrar(searchView.getQuery().toString()));

        // 🔹 Spinner de ordenamiento
        spinnerOrden = view.findViewById(R.id.spinnerOrden);
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Por fecha", "Por lugar", "Por creador"});
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrden.setAdapter(adapterSpinner);
        spinnerOrden.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filtrar(searchView.getQuery().toString());
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // 🔹 Listener en tiempo real
        listenerVoluntariados();

        return view;
    }

    private void listenerVoluntariados() {
        listenerRegistration = db.collection("voluntariados")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(getContext(), "Error al cargar voluntariados", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    listaCompleta.clear();
                    if (queryDocumentSnapshots != null) {
                        for (var doc : queryDocumentSnapshots.getDocuments()) {
                            Voluntariado v = doc.toObject(Voluntariado.class);
                            v.setDocumentId(doc.getId());
                            listaCompleta.add(v);
                        }
                    }
                    filtrar(searchView.getQuery().toString());
                });
    }

    private void filtrar(String texto) {
        String query = texto.toLowerCase().trim();
        String currentUid = (mAuth.getCurrentUser() != null) ? mAuth.getCurrentUser().getUid() : "";

        lista.clear();
        for (Voluntariado v : listaCompleta) {
            boolean coincideTexto =
                    v.getTitulo().toLowerCase().contains(query) ||
                            v.getLugar().toLowerCase().contains(query) ||
                            v.getFecha().toLowerCase().contains(query);

            boolean coincideUsuario = !cbMisVoluntariados.isChecked() ||
                    (v.getUid() != null && v.getUid().equals(currentUid));

            if (coincideTexto && coincideUsuario) {
                lista.add(v);
            }
        }

        // 🔹 Ordenamiento según Spinner
        String criterio = spinnerOrden.getSelectedItem().toString();
        if (criterio.equals("Por fecha")) {
            ordenarPorFecha();
        } else if (criterio.equals("Por lugar")) {
            Collections.sort(lista, Comparator.comparing(Voluntariado::getLugar, String.CASE_INSENSITIVE_ORDER));
        } else if (criterio.equals("Por creador")) {
            Collections.sort(lista, Comparator.comparing(Voluntariado::getUid, String.CASE_INSENSITIVE_ORDER));
        }

        adapter.notifyDataSetChanged();
    }

    private void ordenarPorFecha() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Collections.sort(lista, (v1, v2) -> {
            try {
                return sdf.parse(v1.getFecha()).compareTo(sdf.parse(v2.getFecha()));
            } catch (ParseException e) {
                return 0;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) listenerRegistration.remove();
        fabAdd.hide(); // ocultar FAB al salir del fragment
    }
}