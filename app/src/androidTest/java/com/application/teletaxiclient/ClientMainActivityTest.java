package com.application.teletaxiclient;

import android.content.DialogInterface;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.app.AlertDialog;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.restlet.Client;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dn on 10/07/17.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ClientMainActivityTest {

    @Rule
    public ActivityTestRule<ClientMainActivity> rule = new ActivityTestRule<>(ClientMainActivity.class);

    @Test
    public void shouldUpdateTextAfterButtonClick() {
        onView(withText("Conferma")).perform(click());
        onView(withId(R.id.textHome)).check(matches(withText("Prenotazioni effettuate")));
    }
}
