package com.example.demoilost.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.demoilost.R;
import com.example.demoilost.model.Message;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Message> messages;
    private String currentUserId;

    public ChatAdapter(List<Message> messages) {
        this.messages = messages;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Message message = messages.get(position);
        boolean isMine = message.getSenderId().equals(currentUserId);

        // Handle visibility and content
        if ("image".equals(message.getMessageType())) {
            holder.textView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);
            Glide.with(holder.imageView.getContext())
                    .load(message.getImageUrl())
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(holder.imageView);
        } else {
            holder.textView.setVisibility(View.VISIBLE);
            holder.imageView.setVisibility(View.GONE);
            holder.textView.setText(message.getText());
        }

        // Align message bubble (basic handling via background)
        if (isMine) {
            holder.messageContainer.setBackgroundResource(R.drawable.bg_message_sent);
            holder.messageContainer.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); // align right
        } else {
            holder.messageContainer.setBackgroundResource(R.drawable.bg_message_received);
            holder.messageContainer.setLayoutDirection(View.LAYOUT_DIRECTION_LTR); // align left
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;
        View messageContainer;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.messageTextView);
            imageView = itemView.findViewById(R.id.messageImageView);
            messageContainer = itemView.findViewById(R.id.messageContainer);
        }
    }

    public void updateMessages(List<Message> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }
}
