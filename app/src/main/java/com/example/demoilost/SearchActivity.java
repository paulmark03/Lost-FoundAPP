package com.example.demoilost;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView postsRecyclerView;
    private PostAdapter postAdapter;
    private List<PostModel> postList;

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
                startActivity(new Intent(SearchActivity.this, MessagesActivity.class));
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
        FirebaseFirestore.getInstance().collection("posts")
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


    }
}