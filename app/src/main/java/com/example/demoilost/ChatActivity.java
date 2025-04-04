package com.example.demoilost;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoilost.adapter.ChatAdapter;
import com.example.demoilost.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;




import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText inputMessage;
    private Button sendButton;
    private ImageButton imageButton;
    private ChatAdapter chatAdapter;
    private List<Message> messagesList;
    private FirebaseFirestore firestore;
    private String chatId;
    private String founderId;
    private Uri cameraImageUri;
    private String chat = "chats";

    // Gallery image picker
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        uploadImageToImgur(imageUri);
                    }
                }
            });

    // Camera image capture
    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
                if (result && cameraImageUri != null) {
                    uploadImageToImgur(cameraImageUri);
                }
            });



    public void testSendImageFromDrawable() {
        Uri imageUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.test_image);
        uploadImageToImgur(imageUri);
    }

    boolean isTestMode = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        isTestMode = getIntent().getBooleanExtra("testMode", false);
        if (isTestMode) testSendImageFromDrawable();

        initViews();
        setupChatHeader();
        setupRecyclerView();
        listenForMessages();
        setupSendTextHandler();
        setupSendImageHandler();
    }

    private void initViews() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        inputMessage = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        imageButton = findViewById(R.id.imageButton);
        firestore = FirebaseFirestore.getInstance();

        chatId = getIntent().getStringExtra("chatId");
        founderId = getIntent().getStringExtra("founderId");

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void setupChatHeader() {
        TextView chatHeader = findViewById(R.id.chatHeader);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firestore.collection(chat).document(chatId).get()
                .addOnSuccessListener(chatDoc -> {
                    if (!chatDoc.exists()) return;
                    String userId = chatDoc.getString("userId");
                    String founderInChat = chatDoc.getString("founderId");
                    String otherUserId = currentUserId.equals(userId) ? founderInChat : userId;

                    firestore.collection("users").document(otherUserId).get()
                            .addOnSuccessListener(userDoc -> {
                                String name = userDoc.getString("name");
                                chatHeader.setText("Chat with: " + (name != null ? name : otherUserId));
                            })
                            .addOnFailureListener(e -> chatHeader.setText("Chat with: " + otherUserId));
                });
    }

    private void setupRecyclerView() {
        messagesList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messagesList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);
    }

    private void listenForMessages() {
        firestore.collection(chat)
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e("ChatActivity", "Error loading messages", error);
                        return;
                    }
                    messagesList.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        messagesList.add(doc.toObject(Message.class));
                    }
                    chatAdapter.notifyDataSetChanged();
                    chatRecyclerView.scrollToPosition(messagesList.size() - 1);
                });
    }

    private void setupSendTextHandler() {
        sendButton.setOnClickListener(v -> {
            String messageText = inputMessage.getText().toString().trim();
            if (!messageText.isEmpty()) {
                Message message = new Message(
                        FirebaseAuth.getInstance().getCurrentUser().getUid(),
                        messageText, null, "text", null);
                sendMessageToFirestore(message);
                inputMessage.setText("");
            }
        });
    }

    private void setupSendImageHandler() {
        imageButton.setOnClickListener(v -> {
            String[] options = {"Choose from Gallery", "Take Photo"};
            new android.app.AlertDialog.Builder(ChatActivity.this)
                    .setTitle("Send Photo")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            imagePickerLauncher.launch(intent);
                        } else {
                            File photoFile = new File(getCacheDir(), "captured_" + System.currentTimeMillis() + ".jpg");
                            cameraImageUri = FileProvider.getUriForFile(
                                    ChatActivity.this, getPackageName() + ".provider", photoFile);
                            cameraLauncher.launch(cameraImageUri);
                        }
                    })
                    .show();
        });
    }


    private void sendMessageToFirestore(Message message) {

        firestore.collection(chat)
                .document(chatId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    // Message sent successfully
                    Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
                    chatRecyclerView.scrollToPosition(messagesList.size() - 1);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }




    void uploadImageToImgur(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            byte[] imageBytes = IOUtils.toByteArray(inputStream);
            String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            OkHttpClient client = new OkHttpClient();

            RequestBody body = new FormBody.Builder()
                    .add("image", base64Image)
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.imgur.com/3/image")
                    .header("Authorization", "Client-ID ad8d936a2f446c7")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, java.io.IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(ChatActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String json = response.body().string();
                        try {
                            String imageUrl = new JSONObject(json)
                                    .getJSONObject("data")
                                    .getString("link");

                            Message message = new Message(
                                    FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                    null,
                                    imageUrl,
                                    "image",
                                    null
                            );

                            runOnUiThread(() -> sendMessageToFirestore(message));

                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(() ->
                                    Toast.makeText(ChatActivity.this, "Failed to parse image URL", Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                }

            });

        } catch (Exception e) {
            Toast.makeText(this, "Image processing failed", Toast.LENGTH_SHORT).show();
        }
    }
}
