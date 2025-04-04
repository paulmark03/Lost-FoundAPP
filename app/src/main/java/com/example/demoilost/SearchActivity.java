package com.example.demoilost;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoilost.adapter.PostAdapter;
import com.example.demoilost.model.PostModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private static final String POSTS_COLLECTION = "posts";

    private RecyclerView postsRecyclerView;
    private EditText searchInput;
    private PostAdapter postAdapter;
    private List<PostModel> filteredPostList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        applyInsets();
        initializeViews();
        setupRecyclerView();
        setupBottomNavigation();
        setupSearch();
        loadAllPosts();
    }

    private void applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        searchInput = findViewById(R.id.searchInput);
        postsRecyclerView = findViewById(R.id.postsRecyclerView);
    }

    private void setupRecyclerView() {
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostAdapter(this, filteredPostList);
        postsRecyclerView.setAdapter(postAdapter);
    }

    private void setupBottomNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottom_navigation_view);
        nav.setSelectedItemId(R.id.bottom_search);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.bottom_map) {

                navigateTo(MapActivity.class);
                // Already on map, do nothing
                return true;
            }

            if (id == R.id.bottom_search) {
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

    private void navigateTo(Class<?> cls) {
        NavigationUtils.navigateTo(this, cls);
    }



    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    loadAllPosts();
                } else {
                    searchPosts(keyword);
                }
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }


    private void loadAllPosts() {
        PostRepository.loadAllPosts(filteredPostList, postAdapter,
                () -> Toast.makeText(this, "Failed to load posts", Toast.LENGTH_SHORT).show());
    }


    private void searchPosts(String keyword) {
        PostRepository.searchPosts(keyword, filteredPostList, postAdapter);
    }

}
