package com.example.demoilost;

import android.widget.Toast;

import com.example.demoilost.adapter.PostAdapter;
import com.example.demoilost.model.PostModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class PostRepository {

    private static final String POSTS_COLLECTION = "posts";

    public static void loadAllPosts(List<PostModel> resultList, PostAdapter adapter, Runnable onError) {
        FirebaseFirestore.getInstance().collection(POSTS_COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    resultList.clear();
                    for (DocumentSnapshot doc : querySnapshots) {
                        PostModel post = doc.toObject(PostModel.class);
                        resultList.add(post);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> onError.run());
    }

    public static void searchPosts(String keyword, List<PostModel> resultList, PostAdapter adapter) {
        FirebaseFirestore.getInstance().collection(POSTS_COLLECTION)
                .orderBy("title")
                .startAt(keyword)
                .endAt(keyword + "\uf8ff")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    resultList.clear();
                    for (DocumentSnapshot doc : querySnapshots) {
                        PostModel post = doc.toObject(PostModel.class);
                        resultList.add(post);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
