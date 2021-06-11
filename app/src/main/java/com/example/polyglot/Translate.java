/**
 * REFERENCES:
 * <p>
 * https://stackoverflow.com/questions/53612826/media-player-not-playing-mp3-on-android-api-28 - how to allow emulator with API 28 to produce sound (SproutinGeek, 2019)
 * https://codelabs.developers.google.com/codelabs/android-network-security-config/index.html#3 - forwarded by reference above.
 * https://cloud.ibm.com/apidocs/text-to-speech/text-to-speech?code=java#get-pronunciation - how to stream a pronunciation (IBM,2018).
 * https://stackoverflow.com/questions/25026545/how-to-keep-highlight-of-listview-item-after-device-orientation-changes-in-andro - how to preserve selection of list when rotating screen (Pascual, 2014).
 * https://developer.android.com/guide/topics/resources/runtime-changes - how to handle configuration changes (Android Developers, 2019).
 * https://stackoverflow.com/questions/3234823/android-listview-center-selection - how to set list view selection to be visible after orientation change (The Fettuck, 2014).
 */
package com.example.polyglot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.cloud.sdk.core.http.HttpMediaType;
import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.language_translator.v3.model.TranslateOptions;
import com.ibm.watson.language_translator.v3.model.TranslationResult;
import com.ibm.watson.language_translator.v3.util.Language;
import com.ibm.watson.text_to_speech.v1.model.SynthesizeOptions;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.provider.BaseColumns._ID;
import static com.example.polyglot.MainActivity.data;
import static com.example.polyglot.MainActivity.isConnected;
import static com.example.polyglot.MainActivity.textToSpeech;
import static com.example.polyglot.MainActivity.translationService;
import static com.example.polyglot.MainActivity.voiceSetup;
import static com.example.polyglot.TableSetup.TABLE1_COL1;
import static com.example.polyglot.TableSetup.TABLE1_NAME;
import static com.example.polyglot.TableSetup.TABLE2_COL1;
import static com.example.polyglot.TableSetup.TABLE2_COL2;
import static com.example.polyglot.TableSetup.TABLE2_NAME;
import static com.example.polyglot.TableSetup.TABLE3_COL1;
import static com.example.polyglot.TableSetup.TABLE3_COL2;
import static com.example.polyglot.TableSetup.TABLE3_COL3;
import static com.example.polyglot.TableSetup.TABLE3_NAME;

/**
 * Class that opens Translate Activity:
 *
 * <p>
 * - opens a list view populated with phrases that are saved on database.
 * <br>- each time an item on list view is selected
 * the inner class TranslatePhrase is called. When the translation in the chosen language is completed,
 * the TextView tv is automatically populated.
 * <br>- This has removed the need for pressing a translate button as it is part of the functionality of listView onItemClickListener
 * </p>
 * <p>
 * - opens a splitter that displays languages that the user has subscribed to.
 * each time a new language is selected, it clears the selection from listView.
 * </p>
 * <p>
 * - when Pronounce button is clicked the translated element from the listView gets pronounced in the language which spinner has active.
 * <br>
 * The app translates to almost all languages, whenever there is a problem with the server the user is notified.
 * <br>
 * Languages that are with non-Latin alphabet or are Chinese, Japanese or Arabic will not be pronounced.
 * <br>
 * If the language that is being translated has a particular voice function, the app will switch to that voice.
 * Selected available voices are populated during MainActivity loading procedure.
 * </p>
 * <p>
 * - Translate all enables user to see all stored phrases in the selected language.
 * </p>
 * <br> Orientation changes are handled manually to allow for list selection changes tending to layout orientation specifics.
 * @author dim6ata
 */
public class Translate extends AppCompatActivity {

    private ArrayList<String> checkedLanguages;
    protected static Map<String, String> translatedMap;
    protected static ArrayList<String> phraseList;
    private CustomAdapter adapterList;
    private ListView listView;
    private ArrayAdapter<String> adapterSpinner;
    private Spinner spinner;
    private TextView tv;
    private String selectedLanguage;
    private String selectedCode;
    private String selectedPhrase;
    private String translatedPhrase;
    private int selectedPosition;
    private Map<String, String> checkedMap;
    private String[] SELECT_LANG = {TABLE2_COL1, TABLE2_COL2};
    private String[] SELECT_LANG_REV = {TABLE2_COL2, TABLE2_COL1};
    private String ORDER_BY_NAME = TABLE2_COL2 + " ASC ";
    private String[] SELECT_PHRASE = {_ID, TABLE1_COL1};
    private String ORDER_BY_PHRASE = TABLE1_COL1 + " ASC ";
    private String[] SELECT_TRANSLATION = {TABLE3_COL1, TABLE3_COL3};
    private String ORDER_BY_TRANSLATION = " " + TABLE3_COL1 + " ASC ";
    private String WHERE_TRANSLATION = TABLE3_COL2 + " = ? ";
    private ToastedToast toast;
    private Button translateBtn, pronounceBtn;
    private boolean flag = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);

        listView = findViewById(R.id.list_translate_id);
        spinner = findViewById(R.id.spinner_translate_id);
        tv = findViewById(R.id.translate_tv);
        toast = new ToastedToast(this, 0);
        translateBtn = findViewById(R.id.btn_translate_translate_id);
        pronounceBtn = findViewById(R.id.btn_pronounce_translate_id);


        /**
         * Loading only the first time the activity has been loaded.
         */
        if (!flag) {

            flag = true;

            initaliseLists();
            loadData();
            setupListView();
            setupSpinner();


            /**
             * Loading mode - online or offline:
             */
            if (!isConnected) {//offline

                translateBtn.setEnabled(false);
                pronounceBtn.setEnabled(false);
                pronounceBtn.setBackgroundResource(R.drawable.ic_pronounce_off_foreground);

            } else {//online
                translateBtn.setEnabled(true);
                pronounceBtn.setEnabled(true);
                pronounceBtn.setBackgroundResource(R.drawable.ic_pronounce_foreground);

            }
        }
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);


        if (isConnected) {
            adapterList.notifyDataSetChanged();
            int height = listView.getHeight();
            height = (height == 0 ? 1 : height);//handles if height is 0;
            int itemHeight = listView.getChildAt(0).getHeight();
            listView.setSelectionFromTop(selectedPosition, ((height / 2) - (itemHeight / 2)));
            listView.smoothScrollToPosition(selectedPosition);
            adapterList.notifyDataSetChanged();
        }
    }


    private void initaliseLists() {

        checkedLanguages = new ArrayList<>();
        phraseList = new ArrayList<>();
        checkedMap = new HashMap<>();
    }

    /**
     * method that is used for populating lists with information from database.
     */
    private void loadData() {

        /**
         * loading language elements:
         */


        checkedLanguages = viewSelector(TABLE2_NAME, TABLE2_COL2, SELECT_LANG, ORDER_BY_NAME);


        /**
         * loading phrases:
         */

        phraseList = viewSelector(TABLE1_NAME, TABLE1_COL1, SELECT_PHRASE, ORDER_BY_PHRASE);

        /**
         * loading map with language names and codes.
         */

        checkedMap = viewSelector(TABLE2_NAME, TABLE2_COL2, TABLE2_COL1, SELECT_LANG_REV, null, null, null);

    }

    /**
     * method that is responsible for querying and retrieving information from database.
     *
     * @param table   table element for query.
     * @param column  column to be retrieved.
     * @param select  columns from the table to be queried.
     * @param orderBy detail of how to order the retrieved information.
     * @return This instance is responsible for returning an Array List of selected values.
     */
    public ArrayList<String> viewSelector(String table, String column, String[] select, String orderBy) {

        ArrayList<String> list = new ArrayList<>();
        try {
            Cursor cursor = data.getData(table, select, null, orderBy, null);
            list = data.viewData(cursor, column);

        } catch (Exception e) {

            toast.setToast("There has been a problem with reading from Database", Toast.LENGTH_SHORT);

        }
        return list;

    }

    /**
     * method that is responsible for querying and retrieving information from database.
     *
     * @param table   table element for query.
     * @param column1 first column to be retrieved.
     * @param column2 second column to be retrieved.
     * @param select  columns from the table to be queried
     * @param where   where clause that specifies which element to be selected in a query.
     * @param orderBy detail of how to order the retrieved information.
     * @param arg     argument list of value that follows the where clause.
     * @return This instance is responsible for returning a Map of pairs of values.
     */
    public Map<String, String> viewSelector(String table, String column1, String column2, String[] select, String where, String orderBy, String[] arg) {

        Map<String, String> map = new HashMap<>();
        try {
            Cursor cursor = data.getData(table, select, where, orderBy, arg);
            map = data.viewData(cursor, column1, column2);

        } catch (Exception e) {

            toast.setToast("There has been a problem with reading from Database", Toast.LENGTH_SHORT);

        }
        return map;

    }

    /**
     * method that is used to clear the selection when a new language is selected in spinner.
     */
    private void clearPhraseSelection() {

        tv.setText("");
        translatedPhrase = null;
        adapterList.setSelected(false);
        adapterList.notifyDataSetChanged();

    }

    /**
     * sets up Spinner
     */
    private void setupSpinner() {


        adapterSpinner = new ArrayAdapter<String>(this, R.layout.row_layout_spinner, checkedLanguages) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View view = convertView;
                if (view == null) {
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = inflater.inflate(R.layout.row_layout_spinner, null);

                }
                TextView tv = view.findViewById(R.id.spinner_text_id);
                tv.setText(checkedLanguages.get(position));

                return view;

            }
        };
        adapterSpinner.setDropDownViewResource(R.layout.layout_spinner_dropdown);
        spinner.setAdapter(adapterSpinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                selectedLanguage = parent.getSelectedItem().toString();
                selectedCode = checkedMap.get(selectedLanguage);


                if (isConnected) {
                    clearPhraseSelection();//clears phraseSelection.
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }


    /**
     * Sets up list view
     */
    private void setupListView() {


        adapterList = new CustomAdapter(this, R.layout.row_layout_list, phraseList);
        listView.setAdapter(adapterList);
        listView.setVisibility(View.VISIBLE);

        if (isConnected) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    adapterList.setSelected(true);
                    selectedPosition = position;
                    adapterList.setSelectedPosition(selectedPosition);

                    /**
                     * Removes the need to press translate button to translate phrases.
                     * Translates every time a list view item is selected:
                     */

                    selectedPhrase = adapterList.getItem(selectedPosition);
                    new TranslatePhrase().execute(selectedPhrase);
                    adapterList.notifyDataSetChanged();

                }
            });
        }


    }


    /**
     * click listener for all phrases to be translated to languages selected by user and stored to database.
     *
     * @param view
     */
    public void translateOnClick(View view) {

        toast.setToast("It may take a few seconds to translate phrases to all selected languages. " +
                "You will be notified when it is ready to use!", Toast.LENGTH_LONG);

        data.deleteAll(TABLE3_NAME);
        new TranslateAllPhrases().execute();

    }

    /**
     * click listener for pronounce button, which starts an instance of PronouncePhrase object.
     *
     * @param view
     */
    public void pronounceOnClick(View view) {

        new PronouncePhrase().execute(translatedPhrase, selectedCode);
    }


    /**
     * method that retrieves translation values for a selected language in spinner and starts a new activity to view all phrases.
     *
     * @param view
     */
    public void viewAllOnClick(View view) {

        translatedMap = new HashMap<>();
        String[] arg = {selectedCode};
        translatedMap = viewSelector(TABLE3_NAME, TABLE3_COL1, TABLE3_COL3, SELECT_TRANSLATION, WHERE_TRANSLATION, ORDER_BY_TRANSLATION, arg);

        Intent intent = new Intent(this, ViewAllTranslatedItems.class);
        intent.putExtra("selectedLanguage", selectedLanguage);
        startActivity(intent);

    }

    /**
     * Returns user to home page when logo icon is clicked.
     *
     * @param view
     */
    public void translateReturnHome(View view) {


        Intent intent = new Intent(this, HomeScreen.class);
        startActivity(intent);

    }


    /**
     * A Class that is responsible for translating a phrase from English to a language selected by user.
     */
    private class TranslatePhrase extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... strings) {


            TranslateOptions translateOptions = new TranslateOptions.Builder()
                    .addText(strings[0])
                    .source(Language.ENGLISH)
                    .target(selectedCode)
                    .build();
            String translatedPhrase = "";
            try {
                TranslationResult result = translationService.translate(translateOptions).execute().getResult();
                translatedPhrase = result.getTranslations().get(0).getTranslation();
            } catch (Exception e) {
                translatedPhrase = "There has been a problem with retrieving your translation!";
            } finally {
                return translatedPhrase;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            tv.setText(s);
            translatedPhrase = s;
        }
    }

    /**
     * A Class that is responsible for pronouncing a phrase in a selected language.
     */
    public class PronouncePhrase extends AsyncTask<String, Void, Boolean> {


        @Override
        protected Boolean doInBackground(String... strings) {

            String chosenVoice;
            boolean flag = false;
            if (strings[0] == null) {

                strings[0] = "You need to select a phrase first.";
            }

            if (strings[1] != null && voiceSetup.keySet().contains(strings[1])) {

                chosenVoice = voiceSetup.get(strings[1]);
            } else {
                chosenVoice = voiceSetup.get("en");
            }


            try {
                SynthesizeOptions synthesizeOptions = new SynthesizeOptions.Builder()
                        .text(strings[0])
                        .voice(chosenVoice)
                        .accept(HttpMediaType.AUDIO_WAV)
                        .build();

                InputStream inputStream =
                        textToSpeech.synthesize(synthesizeOptions).execute().getResult();

                StreamPlayer streamPlayer = new StreamPlayer();
                streamPlayer.playStream(inputStream);
                flag = true;
            } catch (Exception e) {
                flag = false;
            } finally {

                return flag;
            }

        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);

            if (!b) {
                toast.setToast("There has been an error in pronouncing the phrase.", Toast.LENGTH_SHORT);
            }

        }
    }


    /**
     * A Class that is responsible for translating all phrases into all languages that have been subscribed to by a user
     * and storing them into the database.
     */
    private class TranslateAllPhrases extends AsyncTask<Void, Void, Boolean> {

        private boolean flag;

        @Override
        protected Boolean doInBackground(Void... voids) {

            flag = false;

            for (int lang = 0; lang < checkedLanguages.size(); lang++) {//loops through the subscribed languages

                flag = groupTranslation(checkedMap.get(checkedLanguages.get(lang)));//sends the value of each key in checked languages.

            }

            return flag;
        }

        /**
         * method that translates all phrases for a given language and adds them to database.
         *
         * @param lang language code that will be used to have the phrases translated to.
         * @return Returns true if the phrases have been successfully added, otherwise returns false.
         */
        protected boolean groupTranslation(String lang) {

            String errorMessage = "Unfortunately, the selected language is not currently supported. Choose another!";
            boolean isAdded = false;

            for (String key : phraseList) {//loops through the list of phrases

                TranslateOptions translateOptions = new TranslateOptions.Builder()
                        .addText(key)
                        .source(Language.ENGLISH)
                        .target(lang)
                        .build();
                String translatedPhrase = "";
                try {
                    TranslationResult result = translationService.translate(translateOptions).execute().getResult();

                    translatedPhrase = result.getTranslations().get(0).getTranslation();

                } catch (Exception e) {
                    translatedPhrase = errorMessage;

                } finally {

                    if (!translatedPhrase.equals(errorMessage)) {//only add in case there has not been an error:

                        isAdded = data.addData(TABLE3_NAME,
                                TABLE3_COL1, TABLE3_COL2, TABLE3_COL3,
                                translatedPhrase, lang, phraseList.get(phraseList.indexOf(key)));

                    }
                }
            }
            return isAdded;
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);

            if (bool) {

                toast.setToast("The data has been updated. " +
                        "You can now press View All to view phrases in selected language", Toast.LENGTH_LONG);
            } else {
                toast.setToast("There has been an error in updating the database.", Toast.LENGTH_SHORT);
            }
        }
    }


}
