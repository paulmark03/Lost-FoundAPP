package com.example.demoilost;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;

public class NavigationUtils {

    public static void navigateTo(Activity activity, Class<?> target) {
        Intent intent = new Intent(activity, target);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.startActivity(intent, ActivityOptions.makeCustomAnimation(
                    activity, R.anim.slide_out_right, R.anim.slide_in_left).toBundle());
        } else {
            activity.startActivity(intent);
        }
        activity.finish();
    }
}
