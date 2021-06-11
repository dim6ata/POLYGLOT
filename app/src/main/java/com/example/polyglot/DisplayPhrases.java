/**
 * REFERENCES:
 * https://developer.android.com/training/data-storage/sqlite#java - how to get indexes of data (Android Developers, 2019).
 * https://stackoverflow.com/questions/7929209/android-sqlite-multiple-tables - how to create multiple tables with foreign keys (kumisku, 2011).
 */

package com.example.polyglot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import static android.provider.BaseColumns._ID;
import static com.example.polyglot.MainActivity.data;
import static com.example.polyglot.TableSetup.TABLE1_COL1;
import static com.example.polyglot.TableSetup.TABLE1_NAME;

import java.util.ArrayList;

/**
 * Class that displays all phrases that have been stored in database.
 * @author dim6ata
 */
public class DisplayPhrases extends AppCompatActivity {

    private ListView listView;
    private CustomAdapter adapter;
    private ArrayList<String> dataList;
    private static String[] SELECT = {_ID, TABLE1_COL1};
    private static String ORDER_BY = TABLE1_COL1 + " ASC ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_phrases);

        dataList = new ArrayList<>();

        try {

            Cursor cursor = data.getData(TABLE1_NAME, SELECT, null, ORDER_BY, null);
            dataList = data.viewData(cursor, TABLE1_COL1);

        } catch (Exception e) {

            new ToastedToast(this, 0).setToast("There has been a problem with reading from Database", Toast.LENGTH_SHORT);
        }


        listView = findViewById(R.id.list_display_id);
        adapter = new CustomAdapter(this, R.layout.row_layout_list, dataList);
        adapter.setSelected(false);
        listView.setAdapter(adapter);
        listView.setVisibility(View.VISIBLE);

    }


    /**
     * Returns user to home page when logo icon is clicked.
     *
     * @param view
     */
    public void displayReturnHome(View view) {

        Intent intent = new Intent(this, HomeScreen.class);
        startActivity(intent);

    }
}
