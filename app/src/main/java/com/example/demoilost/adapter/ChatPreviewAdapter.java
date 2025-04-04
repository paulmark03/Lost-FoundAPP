package com.example.demoilost.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoilost.ChatActivity;
import com.example.demoilost.R;
import com.example.demoilost.model.ChatPreviewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ChatPreviewAdapter extends RecyclerView.Adapter<ChatPreviewAdapter.ChatViewHolder> {

    private Context context;
    private List<ChatPreviewModel> chatList;
    private String currentUserId;

    public ChatPreviewAdapter(Context context, List<ChatPreviewModel> chatList) {
        this.context = context;
        this.chatList = chatList;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_preview, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatPreviewModel chat = chatList.get(position);

        // Determine who the other user is
        String founderId = chat.getFounderId();
        String userId = chat.getUserId();
        String otherUserId = (founderId != null && founderId.equals(currentUserId)) ? userId : founderId;

        if (otherUserId != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(otherUserId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        String name = snapshot.getString("name");
                        holder.chatWithUser.setText("Chat with: " + (name != null ? name : "Unknown User"));
                    })
                    .addOnFailureListener(e -> holder.chatWithUser.setText("Chat with: Unknown User"));
        } else {
            holder.chatWithUser.setText("Chat with: Unknown User");
        }

        // Show the last message instead of the post ID
        String lastMsg = chat.getLastMessage();
        holder.chatPostId.setText("Last message: " + (lastMsg != null ? lastMsg : "No messages yet"));

        // On click → open chat
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("chatId", chat.getChatId());
            intent.putExtra("founderId", chat.getFounderId());
            intent.putExtra("postId", chat.getPostId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView chatWithUser;
        TextView chatPostId;


        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            chatWithUser = itemView.findViewById(R.id.chatWithUser);
            chatPostId = itemView.findViewById(R.id.chatPostId);
        }
    }
}
