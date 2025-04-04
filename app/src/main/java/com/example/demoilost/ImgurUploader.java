package com.example.demoilost; // or your preferred package

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ImgurUploader {

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String errorMessage);
    }

    public static void upload(Context context, Uri imageUri, UploadCallback callback) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(imageUri)) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int nRead;

            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            byte[] imageBytes = buffer.toByteArray();
            String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder().add("image", base64Image).build();

            Request request = new Request.Builder()
                    .url("https://api.imgur.com/3/image")
                    .header("Authorization", "Client-ID ad8d936a2f446c7")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            callback.onFailure("Upload failed: " + e.getMessage()));
                }

                @Override public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String imageUrl = new JSONObject(response.body().string())
                                    .getJSONObject("data").getString("link");
                            new Handler(Looper.getMainLooper()).post(() ->
                                    callback.onSuccess(imageUrl));
                        } catch (JSONException e) {
                            new Handler(Looper.getMainLooper()).post(() ->
                                    callback.onFailure("Failed to parse response"));
                        }
                    } else {
                        new Handler(Looper.getMainLooper()).post(() ->
                                callback.onFailure("Imgur upload failed"));
                    }
                }
            });

        } catch (Exception e) {
            callback.onFailure("Image processing failed: " + e.getMessage());
        }
    }
}
