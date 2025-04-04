package com.example.demoilost;

import android.util.Base64;

import java.io.IOException;
import java.io.InputStream;

public class ChatUtils {

    // Private constructor to prevent instantiation
    private ChatUtils() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }

    public static boolean isValidMessage(String text) {
        return text != null && !text.trim().isEmpty();
    }
}
