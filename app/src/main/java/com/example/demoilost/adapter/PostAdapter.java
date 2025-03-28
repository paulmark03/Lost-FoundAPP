package com.example.demoilost.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoilost.R;
import com.example.demoilost.model.PostModel;
import com.example.demoilost.PostDetailActivity; // Ensure this import points to your detail activity package

import java.util.List;

// Post adapter inflates each row layout, binds the posts 's details and handles user interaction.
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

        // Bind text fields
        holder.nameTextView.setText(post.getName());
        holder.addressTextView.setText(post.getLocation());
        holder.detailsTextView.setText(post.getDescription());

        // Bind image if available
        if (post.getImageBlob() != null) {
            byte[] imageBytes = post.getImageBlob().toBytes();
            Log.d("PostAdapter", "Blob bytes length: " + imageBytes.length);
            if (imageBytes.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                if (bitmap != null) {
                    holder.postImageView.setImageBitmap(bitmap);
                } else {
                    Log.e("PostAdapter", "BitmapFactory.decodeByteArray returned null.");
                    holder.postImageView.setImageResource(R.drawable.ic_profile);
                }
            } else {
                Log.e("PostAdapter", "Blob is empty (0 bytes).");
                holder.postImageView.setImageResource(R.drawable.ic_profile);
            }
        } else {
            Log.e("PostAdapter", "ImageBlob is null.");
            holder.postImageView.setImageResource(R.drawable.ic_profile);
        }


        // Set click listener to launch the detail activity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("name", post.getName());
                intent.putExtra("location", post.getLocation());
                intent.putExtra("description", post.getDescription());
                if (post.getImageBlob() != null) {
                    intent.putExtra("imageBytes", post.getImageBlob().toBytes());
                }
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView postImageView;
        TextView nameTextView, addressTextView, detailsTextView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postImageView = itemView.findViewById(R.id.postImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            addressTextView = itemView.findViewById(R.id.addressTextView);
            detailsTextView = itemView.findViewById(R.id.detailsTextView);
        }
    }
}
