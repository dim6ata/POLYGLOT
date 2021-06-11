/**
 * REFERENCES:
 * <p>
 * https://stackoverflow.com/questions/21347661/android-radio-button-in-custom-list-view - how to setup radio buttons within a ListView (Atwell, 2014).
 */

package com.example.polyglot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.provider.BaseColumns._ID;
import static com.example.polyglot.MainActivity.data;
import static com.example.polyglot.TableSetup.TABLE1_COL1;
import static com.example.polyglot.TableSetup.TABLE1_NAME;

/**
 * Class that is responsible for editing phrases that have been stored on database.
 * It queries the database and populates the list with phrases.
 * When the user selects a radio button next to a phrase and presses the Edit button, that phrase
 * gets copied to the text field where the user can perform changes. Once Save button is pressed, the phrase
 * gets stored to the database.
 * Empty values are not allowed.
 * The new value immediately gets sent to the list view, so that the user can see the changes made.
 * Orientation is handled by storing values in onSaveInstanceState and loading them during onCreate.
 * @author dim6ata
 */
public class EditPhrases extends AppCompatActivity {

    private EditText editText;
    private ListView listView;
    private ArrayList<String> editDataList;
    private ArrayAdapter<String> adapter;
    private static String[] SELECT = {_ID, TABLE1_COL1};
    private static String ORDER_BY = TABLE1_COL1 + " ASC ";
    private int selectedPosition = 0;
    private ToastedToast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_phrases);

        listView = findViewById(R.id.list_edit_id);
        if (savedInstanceState != null) {

            selectedPosition = savedInstanceState.getInt("selectedPosition");
        }

        editText = findViewById(R.id.edit_phrase_id);
        editText.setEnabled(false);
        toast = new ToastedToast(this, 0);

        editDataList = new ArrayList<>();

        populateList();

        setUpAdapter();

    }

    /**
     * Adapter setup, which displays text followed by a radio button.
     */
    private void setUpAdapter() {

        adapter = new ArrayAdapter<String>(this, R.layout.row_layout_radio, editDataList) {


            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = inflater.inflate(R.layout.row_layout_radio, null);

                }
                TextView tv = view.findViewById(R.id.row_text_id);
                tv.setText(editDataList.get(position));
                RadioButton radioButton = view.findViewById(R.id.radio_button);
                radioButton.setChecked(position == selectedPosition);
                listView.smoothScrollToPosition(selectedPosition);
                radioButton.setTag(position);
                radioButton.setOnClickListener(new View.OnClickListener() {
                    /**
                     * is an onClick listener for the radio buttons of the elements in list view.
                     * @param view
                     */
                    @Override
                    public void onClick(View view) {
                        selectedPosition = (Integer) view.getTag();//gets the number of the radio button selected
                        notifyDataSetChanged();

                    }
                });


                return view;
            }
        };

        listView.setAdapter(adapter);
        listView.setVisibility(View.VISIBLE);

    }

    /**
     * retrieves all data from database table 1 - Phrases
     */
    private void populateList() {

        try {
            Cursor cursor = data.getData(TABLE1_NAME, SELECT, null, ORDER_BY, null);
            editDataList = data.viewData(cursor, TABLE1_COL1);


        } catch (Exception e) {
            toast.setToast("There has been a problem with reading from Database", Toast.LENGTH_SHORT);

        }


    }

    /**
     * onclick listener for edit button
     *
     * @param view
     */
    public void editOnClick(View view) {

        editText.setEnabled(true);
        editText.setText(editDataList.get(selectedPosition));

    }

    /**
     * onclick listener for save button
     *
     * @param view
     */
    public void saveOnClick(View view) {

        if (!editText.getText().toString().equals("")) {
            if (data.updateData(TABLE1_NAME, TABLE1_COL1, editText.getText().toString(), editDataList.get(selectedPosition))) {

                toast.setToast(editDataList.get(selectedPosition) + " has been updated to " + editText.getText().toString(), Toast.LENGTH_LONG);
                populateList();

                adapter.notifyDataSetChanged();
                selectedPosition = editDataList.indexOf(editText.getText().toString());
                editText.setText("");
                editText.setHint(R.string.edit_phrases);
                editText.setEnabled(false);

            } else {

                toast.setToast("There has been a problem editing the selected element. ", Toast.LENGTH_SHORT);
            }
        } else {
            toast.setToast("You cannot save an empty line. ", Toast.LENGTH_SHORT);
        }

    }

    /**
     * Returns user to home page when logo icon is clicked.
     *
     * @param view
     */
    public void editReturnHome(View view) {

        Intent intent = new Intent(this, HomeScreen.class);
        startActivity(intent);

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("selectedPosition", selectedPosition);


    }

}
