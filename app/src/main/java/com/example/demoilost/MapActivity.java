package com.example.demoilost;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton searchButton = findViewById(R.id.search_location_button);
        TextView currentLocation = findViewById(R.id.current_location);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        searchButton.setOnClickListener(v -> {
            Intent intent = new Intent(MapActivity.this, BottomSheetActivity.class);
            startActivity(intent);
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.bottom_map) {
                // Already in MapActivity, no action needed
                return true;
            } else if (id == R.id.bottom_search) {
                startActivity(new Intent(MapActivity.this, BottomSheetActivity.class));
                return true;
            } else if (id == R.id.bottom_settings) {
                startActivity(new Intent(MapActivity.this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }
}