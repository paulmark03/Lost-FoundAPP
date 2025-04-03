package com.example.demoilost;

import android.content.Intent;
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
    private List<PostModel> fullPostList = new ArrayList<>();
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
        loadAllPosts(filteredPostList, postAdapter);
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
                startActivity(new Intent(this, MapActivity.class));
            } else if (id == R.id.bottom_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else if (id == R.id.bottom_chat) {
                startActivity(new Intent(this, InboxActivity.class));
            } else {
                return true;
            }
            overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
            finish();
            return true;
        });
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    loadAllPosts(filteredPostList, postAdapter);
                } else {
                    searchPosts(keyword, filteredPostList, postAdapter);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No-op
            }
        });
    }

    private void loadAllPosts(List<PostModel> resultList, PostAdapter adapter) {
        FirebaseFirestore.getInstance().collection(POSTS_COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    resultList.clear();
                    for (DocumentSnapshot doc : querySnapshots) {
                        PostModel post = doc.toObject(PostModel.class);
                        resultList.add(post);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void searchPosts(String keyword, List<PostModel> resultList, PostAdapter adapter) {
        FirebaseFirestore.getInstance().collection(POSTS_COLLECTION)
                .orderBy("title")
                .startAt(keyword)
                .endAt(keyword + "\uf8ff")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    resultList.clear();
                    for (DocumentSnapshot doc : querySnapshots) {
                        PostModel post = doc.toObject(PostModel.class);
                        resultList.add(post);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
