package com.example.polyglot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static com.example.polyglot.MainActivity.data;
import static com.example.polyglot.TableSetup.TABLE1_COL1;
import static com.example.polyglot.TableSetup.TABLE1_NAME;

/**
 * Class that is responsible for running AddPhrases activity, where a user can
 * add new phrases to the local database.
 * When a button Save is pressed then the phrase entered in the Edit Text tab is saved to the database.
 * <br>
 * The user is not allowed to save a phrase if there is nothing entered in the text tab.
 * @author dim6ata
 */
public class AddPhrases extends AppCompatActivity {

    Button save;
    EditText addPhrase;
    ToastedToast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_phrases);

        addPhrase = findViewById(R.id.edit_phrase_id);
        save = findViewById(R.id.btn_save_add);
        toast = new ToastedToast(this, 0);

    }

    /**
     * onclick listener for button used to edit new phrases to database.
     *
     * @param view
     */
    public void addOnClick(View view) {

        if (!addPhrase.getText().toString().equals("")) {
            boolean isAdded = data.addData(TABLE1_NAME, TABLE1_COL1, addPhrase.getText().toString());

            if (isAdded) {

                toast.setToast(addPhrase.getText().toString() + " has been added to the database", Toast.LENGTH_LONG);

            } else {

                toast.setToast("There has been a problem adding to the db. ", Toast.LENGTH_SHORT);

            }
        } else {

            toast.setToast("You cannot add an empty line. ", Toast.LENGTH_SHORT);


        }


        addPhrase.setText("");
    }


    /**
     * Returns user to home page when logo icon is clicked.
     *
     * @param view
     */
    public void addReturnHome(View view) {

        Intent intent = new Intent(this, HomeScreen.class);
        startActivity(intent);
    }



}
