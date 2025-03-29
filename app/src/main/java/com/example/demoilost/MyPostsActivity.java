package com.example.demoilost;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
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
    private final List<PostModel> myPostList = new ArrayList<>();
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        // Setup RecyclerView
        myPostsRecyclerView = findViewById(R.id.myPostsRecyclerView);
        myPostsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostAdapter(this, myPostList);
        myPostsRecyclerView.setAdapter(postAdapter);

        // Get current user ID
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Fetch user-specific posts
        fetchMyPosts();

        // Handle back button
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Enable swipe to delete
        initSwipeToDelete();
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
                        Toast.makeText(this, "Failed to load posts: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void initSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                PostModel post = myPostList.get(position);

                FirebaseFirestore.getInstance().collection("posts")
                        .document(post.getPostId())
                        .delete()
                        .addOnSuccessListener(unused -> {
                            myPostList.remove(position);
                            postAdapter.notifyItemRemoved(position);
                            Toast.makeText(MyPostsActivity.this, "Post deleted", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(MyPostsActivity.this, "Delete failed", Toast.LENGTH_SHORT).show();
                            postAdapter.notifyItemChanged(position); // Revert swipe
                        });
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView,
                                    RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;
                Paint paint = new Paint();
                paint.setColor(Color.parseColor("#FF4444")); // red

                Drawable icon = ContextCompat.getDrawable(MyPostsActivity.this, R.drawable.ic_trash); // your trash icon
                int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;

                // Draw red background
                if (dX < 0) { // swiping left
                    c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                            (float) itemView.getRight(), (float) itemView.getBottom(), paint);

                    // Draw trash icon
                    int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                    int iconRight = itemView.getRight() - iconMargin;
                    int iconTop = itemView.getTop() + iconMargin;
                    int iconBottom = iconTop + icon.getIntrinsicHeight();

                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    icon.draw(c);
                }
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(myPostsRecyclerView);
    }

}
