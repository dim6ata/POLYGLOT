package com.example.polyglot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static com.example.polyglot.Translate.phraseList;
import static com.example.polyglot.Translate.translatedMap;

/**
 * Class that displays all phrases in English and in a selected by the user language.
 * <p>
 * The translated language is retrieved from the database.
 * <br>
 * - when an item is selected in each list view, the corresponding item is highlighted in the other.
 * <br>
 * - it is intended for a user to check their knowledge of a specific language.
 * </p>
 * <br> Orientation changes are handled manually to allow for list selection changes tending to layout orientation specifics.
 * @author dim6ata
 */
public class ViewAllTranslatedItems extends AppCompatActivity {

    private TextView tv;
    private ListView listView1;
    private ListView listView2;
    private CustomAdapter adapterList1;
    private CustomAdapter adapterList2;
    private int selectedPosition;
    private String selectedTranslatedPhrase;
    private String selectedOriginalPhrase;
    private boolean flag = false;
    private int selection1 = 0;
    private int selection2 = 0;
    ArrayList<String> translatedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

            setContentView(R.layout.activity_view_all_translated_items);//loads activity with portrait layout

        } else {

            setContentView(R.layout.activity_view_all_translated_items_landscape);//loads activity with landscape layout

        }

        initialSetup();

    }

    /**
     * performs setup of fields and lists.
     */
    private void initialSetup() {
        Intent intent = getIntent();
        String language = intent.getStringExtra("selectedLanguage");

        tv = findViewById(R.id.tv_viewAll_id);
        tv.setText("Phrases in " + language + ":");

        listView1 = findViewById(R.id.list_view1_viewall_id);
        listView2 = findViewById(R.id.list_view2_viewall_id);

        if (!flag) {

            translatedList = new ArrayList<>(translatedMap.keySet());
            Collections.sort(translatedList);//sorts list alphabetically.
            flag = true;

        }
        adapterList1 = new CustomAdapter(this, R.layout.row_layout_list, translatedList);
        adapterList2 = new CustomAdapter(this, R.layout.row_layout_list, phraseList);

        listViewListenerSetup();

        listView1.setAdapter(adapterList1);
        listView1.setVisibility(View.VISIBLE);

        listView2.setAdapter(adapterList2);
        listView2.setVisibility(View.VISIBLE);

    }

    /**
     * method that deals with orientation changes of ViewAllTranslatedItems activity.
     * As the manifest file has android:configChanges="orientation" added, means that
     * orientation changes need to be handled manually.
     * Here the correct layout is chosen depending on the orientation after the change has occurred.
     *
     * @param newConfig carries the orientation changes that have occurred.
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int height1 = 0, itemHeight1 = 0, height2 = 0, itemHeight2 = 0;

        /**
         * checks for the current orientation:
         * <br> if in portrait, then setContentView is called with portrait layout.
         * <br> else setContentView is called with landscape layout.
         */
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

            /**
             * Calculates heights of list views before they are deleted when calling setContentView.
             */
            adapterList1.notifyDataSetChanged();
            adapterList2.notifyDataSetChanged();

            height1 = listView1.getHeight();
            height1 = (height1 == 0 ? 1 : height1);//handles if height is 0;
            itemHeight1 = listView1.getChildAt(0).getHeight();
            height2 = listView2.getHeight();
            height2 = (height2 == 0 ? 1 : height2);//handles if height is 0;
            itemHeight2 = listView2.getChildAt(0).getHeight();


            setContentView(R.layout.activity_view_all_translated_items);//sets the view to portrait

        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            /**
             * Calculates heights of list views before they are deleted when calling setContentView.
             */
            adapterList1.notifyDataSetChanged();
            adapterList2.notifyDataSetChanged();

            height1 = listView1.getHeight();
            height1 = (height1 == 0 ? 1 : height1);//handles if height is 0;
            itemHeight1 = listView1.getChildAt(0).getHeight();
            height2 = listView2.getHeight();
            height2 = (height2 == 0 ? 1 : height2);//handles if height is 0;
            itemHeight2 = listView2.getChildAt(0).getHeight();

            setContentView(R.layout.activity_view_all_translated_items_landscape);//sets the view to landscape

        }

        /**
         * After setContentView layout items need to be reinitialised.
         * flag being false, the program does not need to create the arraylist - translatedList again.
         */
        initialSetup();


        /**
         * sets selections:
         */
        adapterList1.setSelected(true);
        adapterList2.setSelected(true);
        adapterList1.setSelectedPosition(selection1);
        adapterList2.setSelectedPosition(selection2);

        /**
         * takes care of making selected items visible in list views:
         */
        listView1.setSelectionFromTop(selection1, ((height1 / 2) - (itemHeight1 / 2)));
        listView2.setSelectionFromTop(selection2, ((height2 / 2) - (itemHeight2 / 2)));

        adapterList1.notifyDataSetChanged();
        adapterList2.notifyDataSetChanged();


    }


    /**
     * list view listener setup.
     * - click listeners responsible for performing selection procedures in each list view.
     */
    private void listViewListenerSetup() {
        /**
         * click listener for list view for translated language
         */
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                adapterList1.setSelected(true);
                adapterList2.setSelected(true);
                selectedTranslatedPhrase = adapterList1.getItem(position);
                selectedPosition = adapterList2.getPosition(translatedMap.get(selectedTranslatedPhrase));
                selection1 = position;
                selection2 = selectedPosition;
                adapterList1.setSelectedPosition(position);
                adapterList2.setSelectedPosition(selectedPosition);
                listView2.smoothScrollToPosition(selectedPosition);

                adapterList1.notifyDataSetChanged();
                adapterList2.notifyDataSetChanged();

            }
        });

        /**
         * click listener for list view for initial language(English).
         */
        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                adapterList1.setSelected(true);
                adapterList2.setSelected(true);
                selectedOriginalPhrase = adapterList2.getItem(position);

                String phraseToSelect = getKey(selectedOriginalPhrase, translatedMap);
                selectedPosition = adapterList1.getPosition(phraseToSelect);
                selection1 = selectedPosition;
                selection2 = position;

                adapterList1.setSelectedPosition(selectedPosition);
                adapterList2.setSelectedPosition(position);
                listView1.smoothScrollToPosition(selectedPosition);

                adapterList1.notifyDataSetChanged();
                adapterList2.notifyDataSetChanged();

            }
        });

    }

    /**
     * method that retrieves the key, which holds the translated language equivalent to the selected phrase in English.
     *
     * @param toCompare selected phrase in english.
     * @param map       a map that holds the language values and keys.
     * @return returns the translated language.
     */
    public String getKey(String toCompare, Map<String, String> map) {

        for (String key : map.keySet()) {

            if (map.get(key).equals(toCompare)) {//compares the selected language and the same element in the map.

                return key;//gets the key of the translated language.
            }

        }
        return null;
    }


    /**
     * Returns user to home page when logo icon is clicked.
     *
     * @param view
     */
    public void viewAllReturnHome(View view) {

        Intent intent = new Intent(this, HomeScreen.class);
        startActivity(intent);

    }
}
