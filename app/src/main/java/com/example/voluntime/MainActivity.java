package com.example.voluntime;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Fragment inicial
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new InicioFragment())
                    .commit();
        }

        // Listener de navegación inferior
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            // Se usan if/else en lugar de switch/case
             if (item.getItemId() == R.id.nav_inicio) {
                selectedFragment = new InicioFragment();
            } else if (item.getItemId() == R.id.nav_mis_voluntariados) {
                selectedFragment = new MisVoluntariadosFragment();
            } else if (item.getItemId() == R.id.nav_perfil) {
                selectedFragment = new PerfilFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });
    }
}