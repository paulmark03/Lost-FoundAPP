package com.example.demoilost;

import android.os.IBinder;
import android.view.WindowManager;

import androidx.test.espresso.Root;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class ToastMatcher extends TypeSafeMatcher<Root> {

    @Override
    public void describeTo(Description description) {
        description.appendText("is a Toast");
    }

    @Override
    public boolean matchesSafely(Root root) {
        int type = root.getWindowLayoutParams().get().type;

        // This reliably matches Toasts
        if (type == WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                || type == WindowManager.LayoutParams.TYPE_TOAST) {

            IBinder windowToken = root.getDecorView().getWindowToken();
            IBinder appToken = root.getDecorView().getApplicationWindowToken();

            // Ensures it's not a window over another app
            return windowToken == appToken;
        }

        return false;
    }
}
