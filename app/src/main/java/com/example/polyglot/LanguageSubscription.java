/**
 * REFERENCES:
 * <p>
 * https://stackoverflow.com/questions/12647001/listview-with-custom-adapter-containing-checkboxes - how to use checkboxes inside a list view (LuksProg, 2012).
 * https://stackoverflow.com/questions/32065267/recyclerview-changing-items-during-scroll - how to avoid changing items in list view adapter during scroll (Surendar D, 2016).
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static com.example.polyglot.MainActivity.data;
import static com.example.polyglot.MainActivity.isConnected;
import static com.example.polyglot.MainActivity.langMap;
import static com.example.polyglot.MainActivity.langNames;
import static com.example.polyglot.TableSetup.TABLE2_COL1;
import static com.example.polyglot.TableSetup.TABLE2_COL2;
import static com.example.polyglot.TableSetup.TABLE2_NAME;

/**
 * Class that handles language subscription of user to languages that are available on the IBM Watson Language Translator service.
 * A list of all languages is displayed together with a checkbox next to each line.
 * When a user clicks on a checkbox and presses the Update button, all language names and codes get written to the database.
 * These languages will then be used to get phrases translated to.
 * Orientation is handled by storing values in onSaveInstanceState and loading them during onCreate.
 * @author dim6ata
 */
public class LanguageSubscription extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private CheckBox checkBox;
    private boolean[] checkedArray;
    protected static ArrayList<String> checkedItems;
    private String[] SELECT = {TABLE2_COL1, TABLE2_COL2};
    private String ORDER_BY = TABLE2_COL2 + " ASC ";
    private ToastedToast toast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_subscription);

        toast = new ToastedToast(this, 0);

        /**
         * Handles screen Rotation, by loading values previously saved in onSavedInstanceState.
         * It retrieves the elements in checkedItems and the size of checkedArray and like this
         * the checkboxes that have been selected remain selected.
         */
        if (savedInstanceState != null) {

            checkedArray = new boolean[savedInstanceState.getInt("checkedArrayLength")];
            checkedItems = new ArrayList<>();
            checkedArray = savedInstanceState.getBooleanArray("checkedArray");
            checkedItems = savedInstanceState.getStringArrayList("checkedItems");

        } else {//the first time the activity loads and there is no savedInstanceState.

            setData();

        }


        setAdapter();

    }

    /**
     * retrieves and sets data to class variables.
     * checkedItems receives the names of user selected language items that are stored on the database.
     * checkedArray receives a true or false value depending on whether the language items are on the database.
     */
    private void setData() {

        checkedItems = new ArrayList<>();

        try {
            Cursor cursor = data.getData(TABLE2_NAME, SELECT, null, ORDER_BY, null);
            checkedItems = data.viewData(cursor, TABLE2_COL2);

        } catch (Exception e) {

            toast.setToast("There has been a problem with reading from Database", Toast.LENGTH_SHORT);
        }
        checkedArray = new boolean[langNames.size()];
        for (int i = 0; i < checkedArray.length; i++) {

            checkedArray[i] = checkedItems.contains(langNames.get((i)));

        }
    }

    /**
     * setup of adapter and list view
     */
    private void setAdapter() {

        listView = findViewById(R.id.list_lng_id);
        if (isConnected) {
            adapter = new ArrayAdapter<String>(this, R.layout.row_layout_checkbox, langNames) {


                @Override
                public int getItemViewType(int position) {

                    return position;
                }

                @Override
                public int getViewTypeCount() {

                    return getCount();
                }

                /**
                 * sets up adapter with text followed by a checkbox button.
                 *
                 * @param position
                 * @param convertView
                 * @param parent
                 * @return
                 */
                @Override
                public View getView(final int position, View convertView, ViewGroup parent) {

                    View view = convertView;
                    if (view == null) {
                        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        view = inflater.inflate(R.layout.row_layout_checkbox, null);

                    }
                    final TextView tv = view.findViewById(R.id.row_check_text_id);
                    tv.setText(langNames.get(position));
                    checkBox = view.findViewById(R.id.checkbox_id);

                    checkBox.setTag(Integer.valueOf(position));

                    /**
                     * changes checked items depending on whether a user checks/unchecks them.
                     */
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                            checkedArray[(Integer) buttonView.getTag()] = isChecked;

                            if (isChecked) {//runs when the button is checked
                                if (checkedItems != null && !checkedItems.contains(langNames.get(position))) {//avoids duplication
                                    checkedItems.add(tv.getText().toString());//adds to the list when there are no duplicates.
                                }
                            } else {//runs when the checkbox item is unchecked.
                                checkedItems.remove(tv.getText().toString());//removes items from the list.
                            }
                        }
                    });

                    checkBox.setChecked(checkedArray[position]);//sets the checked/unchecked items.

                    return view;
                }


            };


            listView.setAdapter(adapter);
            listView.setVisibility(View.VISIBLE);

        }

    }

    /**
     * saves updated list to database when Update button is clicked.
     *
     * @param view
     */
    public void updateOnClick(View view) {

        boolean flag = false;

        flag = data.deleteAll(TABLE2_NAME);

        if (!checkedItems.isEmpty()) {

            for (int i = 0; i < checkedItems.size(); i++) {

                flag = data.addData(TABLE2_NAME, TABLE2_COL1, langMap.get(checkedItems.get(i)), TABLE2_COL2, checkedItems.get(i));

            }
        }

        if (flag) {
            toast.setToast("Database has been updated", Toast.LENGTH_SHORT);

        } else {
            toast.setToast("There has been a problem with updating Database", Toast.LENGTH_SHORT);

        }


    }


    /**
     * Returns user to home page when logo icon is clicked.
     *
     * @param view
     */
    public void languageReturnHome(View view) {

        Intent intent = new Intent(this, HomeScreen.class);
        startActivity(intent);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBooleanArray("checkedArray", checkedArray);
        outState.putInt("checkedArrayLength", checkedArray.length);
        outState.putStringArrayList("checkedItems", checkedItems);


    }
}
