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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView postsRecyclerView;
    private PostAdapter postAdapter;
    private List<PostModel> postList;
    private String posts = "posts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.bottom_search);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.bottom_map) {
                startActivity(new Intent(SearchActivity.this, MapActivity.class));
                overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
                finish();
                return true;
            } else if (id == R.id.bottom_search) {
                return true;
            } else if (id == R.id.bottom_settings) {
                startActivity(new Intent(SearchActivity.this, SettingsActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (id == R.id.bottom_chat) {
                startActivity(new Intent(SearchActivity.this, InboxActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });

        postsRecyclerView = findViewById(R.id.postsRecyclerView);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList);
        postsRecyclerView.setAdapter(postAdapter);

        // Fetch data from Firestore
        FirebaseFirestore.getInstance().collection(posts)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    postList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        PostModel post = doc.toObject(PostModel.class);
                        post.setPostId(doc.getId());  //  Set postId manually!
                        postList.add(post);
                    }
                    postAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(SearchActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );

        EditText searchEditText = findViewById(R.id.searchInput);
        RecyclerView searchRecyclerView = findViewById(R.id.postsRecyclerView);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<PostModel> filteredPosts = new ArrayList<>();
        PostAdapter adapter = new PostAdapter(this, filteredPosts);
        searchRecyclerView.setAdapter(adapter);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // This method is intentionally left empty because we don't need to react
                // before the text is changed. If needed later, you can implement logic here.
                // Alternatively, throw new UnsupportedOperationException("Not implemented yet");
            }


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    loadAllPosts(filteredPosts, adapter);
                } else {
                    searchPosts(s.toString(), filteredPosts, adapter);
                }
            }


            @Override
            public void afterTextChanged(Editable s) {
                // This method is intentionally left blank as we don't need to handle
                // events after the text has changed. Add logic here if post-processing
                // is ever needed in the future.
                // Alternatively, throw new UnsupportedOperationException("Not implemented yet");
            }
        });

        loadAllPosts(filteredPosts, adapter);

    }

    private void loadAllPosts(List<PostModel> resultList, PostAdapter adapter) {
        FirebaseFirestore.getInstance().collection(posts)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    resultList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        PostModel post = doc.toObject(PostModel.class);
                        resultList.add(post);
                    }
                    adapter.notifyDataSetChanged();
                });
    }


    private void searchPosts(String keyword, List<PostModel> resultList, PostAdapter adapter) {
        FirebaseFirestore.getInstance().collection(posts)
                .orderBy("title")
                .startAt(keyword)
                .endAt(keyword + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    resultList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        PostModel post = doc.toObject(PostModel.class);
                        resultList.add(post);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

}