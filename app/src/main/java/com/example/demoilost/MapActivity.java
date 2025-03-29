package com.example.demoilost;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap myMap;

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

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ImageButton searchButton = findViewById(R.id.search_location_button);
        ImageButton postButton = findViewById(R.id.post_button);
        TextView currentLocation = findViewById(R.id.current_location);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.bottom_map);

        postButton.setOnClickListener(v ->{
            Intent intent = new Intent(MapActivity.this, PostActivity.class);
            startActivity(intent);
        });

        searchButton.setOnClickListener(v -> {
            Intent intent = new Intent(MapActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.bottom_map) {
                // Already in MapActivity, no action needed
                return true;
            } else if (id == R.id.bottom_search) {
                startActivity(new Intent(MapActivity.this, SearchActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (id == R.id.bottom_settings) {
                startActivity(new Intent(MapActivity.this, SettingsActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (id == R.id.bottom_chat) {
                startActivity(new Intent(MapActivity.this, InboxActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        myMap = googleMap;

        LatLng eindhoven = new LatLng(51.4398, 5.4785);
        myMap.addMarker(new MarkerOptions().position(eindhoven).title("Eindhoven"));
        myMap.moveCamera(CameraUpdateFactory.newLatLng(eindhoven));
    }
}