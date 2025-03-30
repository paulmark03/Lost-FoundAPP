package com.example.demoilost.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.demoilost.PostDetailActivity;
import com.example.demoilost.R;
import com.example.demoilost.model.PostModel;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context context;
    private List<PostModel> postList;

    public PostAdapter(Context context, List<PostModel> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        PostModel post = postList.get(position);

        holder.titleTextView.setText(post.getTitle() != null ? post.getTitle() : "Untitled");
        holder.locationTextView.setText(post.getAddress() != null ? post.getAddress() : "Unknown Location");
        holder.descriptionTextView.setText(post.getDescription() != null ? post.getDescription() : "No description");

        // Load image
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(post.getImageUrl())
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.postImageView);
        } else {
            holder.postImageView.setImageResource(R.drawable.ic_profile);
        }

        // Open PostDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("title", post.getTitle());
            intent.putExtra("address", post.getAddress());
            intent.putExtra("description", post.getDescription());
            intent.putExtra("imageUrl", post.getImageUrl());

            // Ensure geo is passed only if available
            GeoPoint geo = post.getLocation();
            if (geo != null) {
                intent.putExtra("latitude", geo.getLatitude());
                intent.putExtra("longitude", geo.getLongitude());
            }

            // Pass critical values
            intent.putExtra("postId", post.getPostId() != null ? post.getPostId() : "");
            intent.putExtra("founderId", post.getPosterId() != null ? post.getPosterId() : "");

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView postImageView;
        TextView titleTextView, locationTextView, descriptionTextView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postImageView = itemView.findViewById(R.id.postImageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            locationTextView = itemView.findViewById(R.id.addressTextView);
            descriptionTextView = itemView.findViewById(R.id.detailsTextView);
        }
    }
}
