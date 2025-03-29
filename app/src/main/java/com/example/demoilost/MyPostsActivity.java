package com.example.demoilost;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoilost.adapter.PostAdapter;
import com.example.demoilost.model.PostModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyPostsActivity extends AppCompatActivity {

    private RecyclerView myPostsRecyclerView;
    private PostAdapter postAdapter;
    private List<PostModel> myPostList;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        myPostsRecyclerView = findViewById(R.id.myPostsRecyclerView);
        myPostsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        myPostList = new ArrayList<>();
        postAdapter = new PostAdapter(this, myPostList);
        myPostsRecyclerView.setAdapter(postAdapter);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        fetchMyPosts();

        //  Back button logic
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    private void fetchMyPosts() {
        FirebaseFirestore.getInstance().collection("posts")
                .whereEqualTo("posterId", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    myPostList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        PostModel post = doc.toObject(PostModel.class);
                        post.setPostId(doc.getId());
                        myPostList.add(post);
                    }
                    postAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(MyPostsActivity.this, "Failed to load posts: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
