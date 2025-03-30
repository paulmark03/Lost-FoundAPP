package com.example.demoilost;

import android.view.View;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class FirstViewMatcher {
    public static <T> Matcher<T> first(final Matcher<T> matcher) {
        return new TypeSafeMatcher<T>() {
            boolean isFirst = true;

            @Override
            public boolean matchesSafely(T item) {
                if (isFirst && matcher.matches(item)) {
                    isFirst = false;
                    return true;
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("first matching: ");
                matcher.describeTo(description);
            }
        };
    }
}
