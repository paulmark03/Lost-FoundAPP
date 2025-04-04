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

    private RecyclerView postsRecyclerView;
    private PostAdapter postAdapter;
    private final List<PostModel> postList = new ArrayList<>();
    private String currentUserId;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        initViews();
        setupRecyclerView();
        setupSwipeToDelete();
        fetchPosts();
    }

    private void initViews() {
        postsRecyclerView = findViewById(R.id.myPostsRecyclerView);
        firestore = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        postAdapter = new PostAdapter(this, postList);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postsRecyclerView.setAdapter(postAdapter);
    }

    private void fetchPosts() {
        firestore.collection("posts")
                .whereEqualTo("posterId", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    postList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        PostModel post = doc.toObject(PostModel.class);
                        post.setPostId(doc.getId());
                        postList.add(post);
                    }
                    postAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        showToast("Failed to load posts: " + e.getMessage()));
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                PostModel post = postList.get(position);

                firestore.collection("posts").document(post.getPostId())
                        .delete()
                        .addOnSuccessListener(unused -> {
                            postList.remove(position);
                            postAdapter.notifyItemRemoved(position);
                            showToast("Post deleted");
                        })
                        .addOnFailureListener(e -> {
                            showToast("Delete failed: " + e.getMessage());
                            postAdapter.notifyItemChanged(position); // Revert swipe
                        });
            }


            @Override
            public void onChildDraw(Canvas c, RecyclerView rv, RecyclerView.ViewHolder vh, float dX, float dY, int actionState, boolean isActive) {
                drawDeleteBackground(c, vh.itemView, dX);
                super.onChildDraw(c, rv, vh, dX, dY, actionState, isActive);
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(postsRecyclerView);
    }

    private void drawDeleteBackground(Canvas c, View itemView, float dX) {
        if (dX >= 0) return;

        Paint paint = new Paint();
        paint.setColor(Color.RED);
        c.drawRect(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom(), paint);

        Drawable icon = ContextCompat.getDrawable(this, R.drawable.ic_trash);
        if (icon == null) return;

        int margin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int left = itemView.getRight() - margin - icon.getIntrinsicWidth();
        int top = itemView.getTop() + margin;
        int right = itemView.getRight() - margin;
        int bottom = top + icon.getIntrinsicHeight();

        icon.setBounds(left, top, right, bottom);
        icon.draw(c);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
