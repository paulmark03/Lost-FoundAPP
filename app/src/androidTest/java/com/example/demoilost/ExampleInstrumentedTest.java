package com.example.demoilost;

import android.content.Context;
import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;


import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void A1useAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.demoilost", appContext.getPackageName());
    }


    @Test
    public void A2testRegisterEmptyPassword() {
        ActivityScenario.launch(RegisterActivity.class);

        onView(withId(R.id.nameInput)).perform(typeText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.emailInput)).perform(typeText("test@example.com"), closeSoftKeyboard());
        onView(withId(R.id.termsCheckbox)).perform(click());
        onView(withId(R.id.signupButton)).perform(click());

        try {
            Thread.sleep(2000); // Wait 3 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.signupButton)).check(matches(isDisplayed()));

    }

    @Test
    public void A3testRegisterEmptyName() {
        ActivityScenario.launch(RegisterActivity.class);

        onView(withId(R.id.emailInput)).perform(typeText("test@example.com"), closeSoftKeyboard());
        onView(withId(R.id.passwordInput)).perform(typeText("validpass123"), closeSoftKeyboard());
        onView(withId(R.id.confirmPasswordInput)).perform(typeText("validpass123"), closeSoftKeyboard());
        onView(withId(R.id.termsCheckbox)).perform(click());
        onView(withId(R.id.signupButton)).perform(click());

        try {
            Thread.sleep(2000); // Wait 3 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.signupButton)).check(matches(isDisplayed()));
    }

    @Test
    public void A4testRegisterEmptyEmail() {
        ActivityScenario.launch(RegisterActivity.class);

        onView(withId(R.id.nameInput)).perform(typeText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.passwordInput)).perform(typeText("validpass123"), closeSoftKeyboard());
        onView(withId(R.id.confirmPasswordInput)).perform(typeText("validpass123"), closeSoftKeyboard());
        onView(withId(R.id.termsCheckbox)).perform(click());
        onView(withId(R.id.signupButton)).perform(click());

        try {
            Thread.sleep(2000); // Wait 3 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.signupButton)).check(matches(isDisplayed()));
    }

    @Test
    public void A5testRegisterUncheckedTermsCheckbox() {
        ActivityScenario.launch(RegisterActivity.class);

        onView(withId(R.id.nameInput)).perform(typeText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.emailInput)).perform(typeText("test@example.com"), closeSoftKeyboard());
        onView(withId(R.id.passwordInput)).perform(typeText("validpass123"), closeSoftKeyboard());
        onView(withId(R.id.confirmPasswordInput)).perform(typeText("validpass123"), closeSoftKeyboard());

        onView(withId(R.id.signupButton)).perform(click());

        try {
            Thread.sleep(2000); // Wait 3 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.signupButton)).check(matches(isDisplayed()));

    }

    @Test
    public void A6testRegisterInvalidEmail() {
        ActivityScenario.launch(RegisterActivity.class);

        onView(withId(R.id.nameInput)).perform(typeText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.emailInput)).perform(typeText("invalid-email"), closeSoftKeyboard());
        onView(withId(R.id.passwordInput)).perform(typeText("validpass123"), closeSoftKeyboard());
        onView(withId(R.id.confirmPasswordInput)).perform(typeText("validpass123"), closeSoftKeyboard());
        onView(withId(R.id.termsCheckbox)).perform(click());

        onView(withId(R.id.signupButton)).perform(click());

        try {
            Thread.sleep(2000); // Wait 3 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.signupButton)).check(matches(isDisplayed()));
    }


    /////////

    // AC001: Register with valid credentials
    @Test
    public void A7testRegisterUserSuccess() {
        ActivityScenario.launch(com.example.demoilost.RegisterActivity.class);

        onView(withId(R.id.nameInput)).perform(typeText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.emailInput)).perform(typeText("newuser@example.com"), closeSoftKeyboard());
        onView(withId(R.id.passwordInput)).perform(typeText("validpass123"), closeSoftKeyboard());
        onView(withId(R.id.confirmPasswordInput)).perform(typeText("validpass123"), closeSoftKeyboard());
        onView(withId(R.id.termsCheckbox)).perform(click());
        onView(withId(R.id.signupButton)).perform(click());

        // TEMP: Wait for transition to LoginActivity
        try {
            Thread.sleep(2000); // Wait 3 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.loginButton)).check(matches(isDisplayed()));
    }

    // AC002: Login with valid credentials
    @Test
    public void A8testLoginSuccess() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.email)).perform(typeText("newuser@example.com"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("validpass123"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        try {
            Thread.sleep(2000); // Wait 3 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        onView(withId(R.id.map)).check(matches(isDisplayed()));
    }

    //AC03
    @Test
    public void A9testLoginEmptyEmail() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.password)).perform(typeText("validpass123"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        try {
            Thread.sleep(2000); // Wait 3 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.email)).check(matches(hasErrorText("Email cannot be empty")));
    }

    @Test
    public void B0testLoginEmptyPassword() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.email)).perform(typeText("newuser@example.com"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        try {
            Thread.sleep(2000); // Wait 3 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.password)).check(matches(hasErrorText("Password cannot be empty")));
    }


    @Test
    public void B1testLoginWrongCredentials() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.email)).perform(typeText("wrong@email.com"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("wrongpass"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());

        // Wait briefly for UI to respond
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check that the login screen is still visible (user not redirected)
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()));
    }

    @Test
    public void B2testUpdateUserNameSuccess() {
        // Launch ManageAccountActivity (simulate user already logged in)
        ActivityScenario.launch(ManageAccountActivity.class);

        // Clear existing and type new name
        onView(withId(R.id.nameEditText))
                .perform(ViewActions.replaceText("New Test Name"), closeSoftKeyboard());

        // Click save button
        onView(withId(R.id.saveNameButton)).perform(click());

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), SettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Required for launching activity in test
        ApplicationProvider.getApplicationContext().startActivity(intent);

        onView(withId(R.id.profileName))
                .check(matches(withText("New Test Name")));


    }

    @Test
    public void B3testDeleteAccountFlow() {
        ActivityScenario.launch(ManageAccountActivity.class);

        // Click the delete button
        onView(withId(R.id.deleteAccountButton)).perform(click());
        // Confirm the dialog
        onView(withText("Delete")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());

        try {
            Thread.sleep(2000); // Wait 3 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Confirm we are redirected to login screen (by checking for login button)
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()));
    }



}
