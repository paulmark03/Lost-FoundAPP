package com.example.demoilost;

import android.app.ActivityOptions;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.demoilost.model.PostModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap myMap;
    private static final String TAG = "MapActivity";
    private ImageButton searchButton;
    private ImageButton postButton;
    private BottomNavigationView bottomNavigationView;
    private TextView currentLocationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);

        setupInsets();
        initViews();
        setupMap();
        setupListeners();
    }

    private void setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        searchButton = findViewById(R.id.search_location_button);
        postButton = findViewById(R.id.post_button);
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        currentLocationTextView = findViewById(R.id.current_location);
        bottomNavigationView.setSelectedItemId(R.id.bottom_map);
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "Map fragment is null");
        }
    }

    private void setupListeners() {
        postButton.setOnClickListener(v -> startActivity(new Intent(this, PostActivity.class)));

        searchButton.setOnClickListener(v -> showSearchDialog());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.bottom_map) {
                // Already on map, do nothing
                return true;
            }

            if (id == R.id.bottom_search) {
                navigateTo(SearchActivity.class);
                return true;
            }

            if (id == R.id.bottom_settings) {
                navigateTo(SettingsActivity.class);
                return true;
            }

            if (id == R.id.bottom_chat) {
                navigateTo(InboxActivity.class);
                return true;
            }

            return false;
        });

    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search Location");

        final EditText input = new EditText(this);
        input.setHint("Type an address or place");
        builder.setView(input);

        builder.setPositiveButton("Search", (dialog, which) -> {
            String address = input.getText().toString().trim();
            if (!address.isEmpty()) {
                searchLocationAndZoom(address);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void navigateTo(Class<?> cls) {
        NavigationUtils.navigateTo(this, cls);
    }



    private void searchLocationAndZoom(String addressText) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            geocoder.getFromLocationName(addressText, 1, new Geocoder.GeocodeListener() {
                @Override
                public void onGeocode(@NonNull List<Address> addresses) {
                    if (!addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        runOnUiThread(() -> {
                            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                            currentLocationTextView.setText(addressText);
                        });
                    } else {
                        runOnUiThread(() -> showToast("No location found for: " + addressText));
                    }
                }

                @Override
                public void onError(@NonNull String errorMessage) {
                    runOnUiThread(() -> showToast("Geocoder error: " + errorMessage));
                }
            });
        } else {
            // Legacy fallback for API < 33 (must run on background thread)
            new Thread(() -> {
                try {
                    List<Address> addresses = geocoder.getFromLocationName(addressText, 1);
                    if (!addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        runOnUiThread(() -> {
                            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                            currentLocationTextView.setText(addressText);
                        });
                    } else {
                        runOnUiThread(() -> showToast("No location found for: " + addressText));
                    }
                } catch (IOException e) {
                    runOnUiThread(() -> showToast("Error finding location: " + e.getMessage()));
                }
            }).start();
        }
    }



    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        myMap.getUiSettings().setZoomGesturesEnabled(true);
        loadPostMarkers();
    }

    private void loadPostMarkers() {
        FirebaseFirestore.getInstance().collection("posts")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        try {
                            PostModel post = doc.toObject(PostModel.class);
                            GeoPoint location = post.getLocation();
                            if (location != null) {
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                myMap.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .title(post.getTitle())
                                        .snippet(post.getDescription()));
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error displaying marker", e);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch posts", e));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
