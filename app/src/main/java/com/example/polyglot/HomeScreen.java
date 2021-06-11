package com.example.polyglot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Class that displays the home screen which contains buttons that act as menu options,
 * allowing user to navigate between activities.
 * <br>In all other activities, when logo is pressed it will return user to this activity.
 * @author dim6ata
 */
public class HomeScreen extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

    }


    /**
     * onclick listener for Add button
     *
     * @param view
     */
    public void addOnClick(View view) {

        startNewActivity(AddPhrases.class);


    }

    /**
     * onclick listener for Display button
     *
     * @param view
     */
    public void displayOnClick(View view) {

        startNewActivity(DisplayPhrases.class);
    }

    /**
     * onclick listener for Edit button
     *
     * @param view
     */
    public void editOnClick(View view) {

        startNewActivity(EditPhrases.class);
    }

    /**
     * onclick listener for Language button
     *
     * @param view
     */
    public void languageOnClick(View view) {

        startNewActivity(LanguageSubscription.class);
    }

    /**
     * onclick listener for Translate button
     *
     * @param view
     */
    public void translateOnClick(View view) {

        startNewActivity(Translate.class);
    }

    /**
     * is used to start a new activity from main.
     *
     * @param className accepts a class parameter
     */
    public void startNewActivity(Class className) {

        Intent intent = new Intent(this, className);
        startActivity(intent);

    }
}
