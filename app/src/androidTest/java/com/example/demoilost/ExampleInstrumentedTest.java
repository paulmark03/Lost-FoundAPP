package com.example.demoilost;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;


import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static androidx.test.espresso.Espresso.onIdle;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {




    private void loginWithTestUser() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.email)).perform(typeText("newuser@example.com"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("validpass123"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        // Optionally wait for navigation to MapActivity
        onIdle(); // Waits for the main looper to be idle

    }


    public Uri getImageFromAssets(Context context, String fileName) {
        try {
            File file = new File(context.getCacheDir(), fileName);
            if (!file.exists()) {
                InputStream inputStream = context.getAssets().open(fileName);
                java.io.FileOutputStream outputStream = new java.io.FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.close();
                inputStream.close();
            }
            return FileProvider.getUriForFile(context,
                    context.getPackageName() + ".provider",
                    file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }




    @Test
    public void A1useAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.demoilost", appContext.getPackageName());
    }


    //AC008
    @Test
    public void A2testRegisterEmptyPassword() {
        ActivityScenario.launch(RegisterActivity.class);

        onView(withId(R.id.nameInput)).perform(typeText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.emailInput)).perform(typeText("test@example.com"), closeSoftKeyboard());
        onView(withId(R.id.termsCheckbox)).perform(click());
        onView(withId(R.id.signupButton)).perform(click());

        onIdle(); // Waits for the main looper to be idle

        onView(withId(R.id.signupButton)).check(matches(isDisplayed()));

    }

    //AC008
    @Test
    public void A3testRegisterEmptyName() {
        ActivityScenario.launch(RegisterActivity.class);

        onView(withId(R.id.emailInput)).perform(typeText("test@example.com"), closeSoftKeyboard());
        onView(withId(R.id.passwordInput)).perform(typeText("validpass123"), closeSoftKeyboard());
        onView(withId(R.id.confirmPasswordInput)).perform(typeText("validpass123"), closeSoftKeyboard());
        onView(withId(R.id.termsCheckbox)).perform(click());
        onView(withId(R.id.signupButton)).perform(click());

        onIdle(); // Waits for the main looper to be idle

        onView(withId(R.id.signupButton)).check(matches(isDisplayed()));
    }

    //AC008
    @Test
    public void A4testRegisterEmptyEmail() {
        ActivityScenario.launch(RegisterActivity.class);

        onView(withId(R.id.nameInput)).perform(typeText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.passwordInput)).perform(typeText("validpass123"), closeSoftKeyboard());
        onView(withId(R.id.confirmPasswordInput)).perform(typeText("validpass123"), closeSoftKeyboard());
        onView(withId(R.id.termsCheckbox)).perform(click());
        onView(withId(R.id.signupButton)).perform(click());

        onIdle(); // Waits for the main looper to be idle

        onView(withId(R.id.signupButton)).check(matches(isDisplayed()));
    }

    //AC008
    @Test
    public void A5testRegisterUncheckedTermsCheckbox() {
        ActivityScenario.launch(RegisterActivity.class);

        onView(withId(R.id.nameInput)).perform(typeText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.emailInput)).perform(typeText("test@example.com"), closeSoftKeyboard());
        onView(withId(R.id.passwordInput)).perform(typeText("validpass123"), closeSoftKeyboard());
        onView(withId(R.id.confirmPasswordInput)).perform(typeText("validpass123"), closeSoftKeyboard());

        onView(withId(R.id.signupButton)).perform(click());

        onIdle(); // Waits for the main looper to be idle

        onView(withId(R.id.signupButton)).check(matches(isDisplayed()));

    }

    //AC008
    @Test
    public void A6testRegisterInvalidEmail() {
        ActivityScenario.launch(RegisterActivity.class);

        onView(withId(R.id.nameInput)).perform(typeText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.emailInput)).perform(typeText("invalid-email"), closeSoftKeyboard());
        onView(withId(R.id.passwordInput)).perform(typeText("validpass123"), closeSoftKeyboard());
        onView(withId(R.id.confirmPasswordInput)).perform(typeText("validpass123"), closeSoftKeyboard());
        onView(withId(R.id.termsCheckbox)).perform(click());

        onView(withId(R.id.signupButton)).perform(click());

        onIdle(); // Waits for the main looper to be idle


        onView(withId(R.id.signupButton)).check(matches(isDisplayed()));
    }

    //AC001
    @Test
    public void A7testRegisterUserSuccess() {
        ActivityScenario.launch(com.example.demoilost.RegisterActivity.class);

        onView(withId(R.id.nameInput)).perform(typeText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.emailInput)).perform(typeText("newuser@example.com"), closeSoftKeyboard());
        onView(withId(R.id.passwordInput)).perform(typeText("validpass123"), closeSoftKeyboard());
        onView(withId(R.id.confirmPasswordInput)).perform(typeText("validpass123"), closeSoftKeyboard());
        onView(withId(R.id.termsCheckbox)).perform(click());
        onView(withId(R.id.signupButton)).perform(click());

        onIdle(); // Waits for the main looper to be idle


        onView(withId(R.id.loginButton)).check(matches(isDisplayed()));
    }

    //AC009
    @Test
    public void A8testRegisterUserEmailUsed() {
        ActivityScenario.launch(com.example.demoilost.RegisterActivity.class);

        onView(withId(R.id.nameInput)).perform(typeText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.emailInput)).perform(typeText("newuser@example.com"), closeSoftKeyboard());
        onView(withId(R.id.passwordInput)).perform(typeText("validpass123"), closeSoftKeyboard());
        onView(withId(R.id.confirmPasswordInput)).perform(typeText("validpass123"), closeSoftKeyboard());
        onView(withId(R.id.termsCheckbox)).perform(click());
        onView(withId(R.id.signupButton)).perform(click());
        onIdle(); // Waits for the main looper to be idle


        onView(withId(R.id.signupButton)).check(matches(isDisplayed()));
    }

    //AC003
    @Test
    public void A9testLoginWrongCredentials() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.email)).perform(typeText("wrong@email.com"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("wrongpass"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());
        onIdle(); // Waits for the main looper to be idle


        // Check that the login screen is still visible (user not redirected)
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()));
    }

    //AC003
    @Test
    public void B0testLoginEmptyEmail() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.password)).perform(typeText("validpass123"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        onIdle(); // Waits for the main looper to be idle


        onView(withId(R.id.email)).check(matches(hasErrorText("Email cannot be empty")));
    }

    //AC003
    @Test
    public void B1testLoginEmptyPassword() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.email)).perform(typeText("newuser@example.com"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        onIdle(); // Waits for the main looper to be idle


        onView(withId(R.id.password)).check(matches(hasErrorText("Password cannot be empty")));
    }


    //AC002
    @Test
    public void B2testLoginSuccess() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.email)).perform(typeText("newuser@example.com"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("validpass123"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        onIdle(); // Waits for the main looper to be idle



        onView(withId(R.id.map)).check(matches(isDisplayed()));
    }

    //AC005
    @Test
    public void B3testUpdateUserNameSuccess() {
        // Launch ManageAccountActivity (simulate user already logged in)
        ActivityScenario.launch(ManageAccountActivity.class);

        // Clear existing and type new name
        onView(withId(R.id.nameEditText))
                .perform(replaceText("New Test Name"), closeSoftKeyboard());

        // Click save button
        onView(withId(R.id.saveNameButton)).perform(click());

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), SettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Required for launching activity in test
        ApplicationProvider.getApplicationContext().startActivity(intent);

        onView(withId(R.id.profileName))
                .check(matches(withText("New Test Name")));


    }

    //LF001
    @Test
    public void B4testCreatePostWithAllDetails() {
        Intents.init();

        loginWithTestUser();

        ActivityScenario<PostActivity> scenario = ActivityScenario.launch(PostActivity.class);

        scenario.onActivity(activity -> {
            activity.setTestLocation("Main Street, London", 51.5074, -0.1278);

            Uri fakeUri = Uri.parse("android.resource://com.example.demoilost/drawable/test_image");
            activity.setTestImage(fakeUri);
        });

        onView(withId(R.id.nameEditText)).perform(typeText("Lost Black Wallet"), closeSoftKeyboard());
        onView(withId(R.id.descriptionEditText)).perform(typeText("Found near the station"), closeSoftKeyboard());
        onView(withId(R.id.postButton)).perform(click());

        onIdle(); // Waits for the main looper to be idle

        onView(withId(R.id.map)).check(matches(isDisplayed()));

        Intents.release();
    }

    //LF003
    @Test
    public void B5testCreatePostMissingTitle() {
        Intents.init();

        loginWithTestUser();
        ActivityScenario<PostActivity> scenario = ActivityScenario.launch(PostActivity.class);

        scenario.onActivity(activity -> {
            activity.setTestLocation("Main Street, London", 51.5074, -0.1278);

            Uri fakeUri = Uri.parse("android.resource://com.example.demoilost/drawable/test_image");
            activity.setTestImage(fakeUri);
        });

        onView(withId(R.id.descriptionEditText)).perform(typeText("Found near the station"), closeSoftKeyboard());
        onView(withId(R.id.postButton)).perform(click());
        onIdle(); // Waits for the main looper to be idle

        onView(withId(R.id.postButton)).check(matches(isDisplayed()));

        Intents.release();
    }

    //LF003
    @Test
    public void B6testCreatePostMissingDescription() {
        Intents.init();

        loginWithTestUser();
        ActivityScenario<PostActivity> scenario = ActivityScenario.launch(PostActivity.class);

        scenario.onActivity(activity -> {
            activity.setTestLocation("Main Street, London", 51.5074, -0.1278);

            Uri fakeUri = Uri.parse("android.resource://com.example.demoilost/drawable/test_image");
            activity.setTestImage(fakeUri);
        });

        onView(withId(R.id.nameEditText)).perform(typeText("Lost Black Wallet"), closeSoftKeyboard());
        onView(withId(R.id.postButton)).perform(click());

        onIdle(); // Waits for the main looper to be idle

        onView(withId(R.id.postButton)).check(matches(isDisplayed()));

        Intents.release();
    }

    //LF003
    @Test
    public void B7testCreatePostMissingLocation() {
        Intents.init();

        loginWithTestUser();
        ActivityScenario<PostActivity> scenario = ActivityScenario.launch(PostActivity.class);

        scenario.onActivity(activity -> {

            Uri fakeUri = Uri.parse("android.resource://com.example.demoilost/drawable/test_image");
            activity.setTestImage(fakeUri);
        });

        onView(withId(R.id.nameEditText)).perform(typeText("Lost Black Wallet"), closeSoftKeyboard());
        onView(withId(R.id.descriptionEditText)).perform(typeText("Found near the station"), closeSoftKeyboard());
        onView(withId(R.id.postButton)).perform(click());

        onIdle(); // Waits for the main looper to be idle

        onView(withId(R.id.postButton)).check(matches(isDisplayed()));

        Intents.release();
    }

    //LF003
    @Test
    public void B8testCreatePostMissingImage() {
        Intents.init();

        loginWithTestUser();
        ActivityScenario<PostActivity> scenario = ActivityScenario.launch(PostActivity.class);

        scenario.onActivity(activity -> {
            activity.setTestLocation("Main Street, London", 51.5074, -0.1278);
        });

        onView(withId(R.id.nameEditText)).perform(typeText("Lost Black Wallet"), closeSoftKeyboard());
        onView(withId(R.id.descriptionEditText)).perform(typeText("Found near the station"), closeSoftKeyboard());
        onView(withId(R.id.postButton)).perform(click());
        onIdle(); // Waits for the main looper to be idle

        onView(withId(R.id.postButton)).check(matches(isDisplayed()));

        Intents.release();
    }


    //AC006
    @Test
    public void B9testDeleteAccountFlow() {

        loginWithTestUser();
        ActivityScenario.launch(ManageAccountActivity.class);

        // Click the delete button
        onView(withId(R.id.deleteAccountButton)).perform(click());
        // Confirm the dialog
        onView(withText("Delete")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());

        onIdle(); // Waits for the main looper to be idle

        // Confirm we are redirected to login screen (by checking for login button)
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()));
    }

    //SR001
    @Test
    public void C0testReportedItemsListViewIsDisplayed() {
        ActivityScenario.launch(SearchActivity.class);

        onIdle(); // Waits for the main looper to be idle


        // Confirm RecyclerView is displayed

        onView(withId(R.id.postsRecyclerView)).check(matches(isDisplayed()));

    }


    //SR002
    @Test
    public void C1testDisplayMap() {
        ActivityScenario.launch(MapActivity.class);

        // Give GoogleMap time to load and attach
        onIdle(); // Waits for the main looper to be idle

        // Just confirm the map view is visible
        onView(withId(R.id.map)).check(matches(isDisplayed()));
    }

    //SR003
    @Test
    public void C2testViewItemDetails() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.email)).perform(typeText("test@test.com"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("dev123"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());
        onIdle(); // Waits for the main looper to be idle


        onView(withId(R.id.bottom_search)).perform(click());

        onIdle(); // Waits for the main looper to be idle


        onView(FirstViewMatcher.first(allOf(
                withId(R.id.titleTextView),
                isDescendantOfA(withId(R.id.postsRecyclerView))
        ))).perform(click());


        onIdle(); // Waits for the main looper to be idle

        // Assert detail screen is shown
        onView(withId(R.id.detailNameTextView)).check(matches(isDisplayed()));
    }


    //DM004
    @Test
    public void C3testStartChatFromItemListing() {
        ActivityScenario.launch(LoginActivity.class);

        // Log in
        onView(withId(R.id.email)).perform(typeText("test@test.com"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("dev123"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        onIdle(); // Waits for the main looper to be idle


        // Navigate to Search tab
        onView(withId(R.id.bottom_search)).perform(click());

        onIdle(); // Waits for the main looper to be idle


        // Click first visible post title
        onView(FirstViewMatcher.first(allOf(
                withId(R.id.titleTextView),
                isDescendantOfA(withId(R.id.postsRecyclerView))
        ))).perform(click());

        onIdle(); // Waits for the main looper to be idle


        // Tap the "Chat with Founder" button
        onView(withId(R.id.chatButton)).perform(click());

        onIdle(); // Waits for the main looper to be idle


        // Send message in chat
        String message = "Interested in this item!";
        onView(withId(R.id.messageInput)).perform(typeText(message), closeSoftKeyboard());
        onView(withId(R.id.sendButton)).perform(click());

        onIdle(); // Waits for the main looper to be idle


        onView(withText(message)).check(matches(isDisplayed()));
    }

    //DM002
    @Test
    public void C4testMessagingSendImage() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.email)).perform(typeText("test@test.com"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("dev123"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        onIdle();
        onView(withId(R.id.bottom_chat)).perform(click());
        onIdle();

        // Click the first visible chat to open ChatActivity
        onView(FirstViewMatcher.first(allOf(
                withId(R.id.chatPostId),
                isDescendantOfA(withId(R.id.messageRecyclerView))
        ))).perform(click());

        onIdle();

        // Get ChatActivity instance from foreground
        final ChatActivity[] activityRef = new ChatActivity[1];
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Collection<Activity> activities = ActivityLifecycleMonitorRegistry
                    .getInstance()
                    .getActivitiesInStage(Stage.RESUMED);
            for (Activity activity : activities) {
                if (activity instanceof ChatActivity) {
                    activityRef[0] = (ChatActivity) activity;
                    break;
                }
            }
        });

        ChatActivity activity = activityRef[0];
        assertNotNull("ChatActivity not found", activity);

        // Simulate sending image from drawable using real chatId
        Uri testImageUri = Uri.parse("android.resource://" + activity.getPackageName() + "/" + R.drawable.test_image);
        activity.uploadImageToImgur(testImageUri);

        onIdle(); // Wait for upload + Firestore write

        // Verify image is shown in chat
        onView(withId(R.id.chatRecyclerView))
                .check(matches(hasDescendant(withContentDescription("chat_image"))));
        onView(withId(R.id.chatRecyclerView)).check(matches(hasDescendant(withContentDescription("chat_image"))));
    }


    //DM001
    @Test
    public void C5testMessagingViewSendDelete() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.email)).perform(typeText("test@test.com"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("dev123"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        onIdle(); // Waits for the main looper to be idle

        onView(withId(R.id.bottom_chat)).perform(click());

        onIdle(); // Waits for the main looper to be idle


        // Click any visible post title to navigate
        onView(FirstViewMatcher.first(allOf(
                withId(R.id.chatPostId),
                isDescendantOfA(withId(R.id.messageRecyclerView))
        ))).perform(click());



        // Step 1: Send a message
        String testMessage = "Hello Espresso";

        onView(withId(R.id.messageInput)).perform(typeText(testMessage), closeSoftKeyboard());
        onView(withId(R.id.sendButton)).perform(click());

        // Step 2: Check that the message appears
        onIdle(); // Waits for the main looper to be idle


        onView(withText(testMessage)).check(matches(isDisplayed()));

        onIdle(); // Waits for the main looper to be idle


        onView(withId(R.id.backButton)).perform(click());

        onView(allOf(withId(R.id.chatPostId)))
                .perform(ViewActions.swipeLeft());

        // Wait for animation/deletion
        onIdle(); // Waits for the main looper to be idle


        // Confirm it's gone
        onView(withId(R.id.messageTextView)).check(doesNotExist());

    }


    //MP001
    @Test
    public void C6testMapSearchCity() {
        ActivityScenario.launch(LoginActivity.class);

        // Log in
        onView(withId(R.id.email)).perform(typeText("test@test.com"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("dev123"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        // Wait for main activity to load
        onIdle(); // Waits for the main looper to be idle


        // Type city name in search input
        onView(withId(R.id.search_location_button)).perform(click());

        // Click search button
        // Type in city name in the dialog input
        onView(withClassName(containsString("EditText")))
                .perform(typeText("Paris"), closeSoftKeyboard());

        // Click the "Search" button
        onView(withText("Search")).perform(click());


        // Wait for map to update
        onIdle(); // Waits for the main looper to be idle


        // Check if map shows city result - assume you set a marker or update a text view
        onView(withText(containsString("Paris"))).check(matches(isDisplayed()));
    }


}
