/**
 * REFERENCES:
 * <p>
 * https://stackoverflow.com/questions/17357226/add-the-loading-screen-in-starting-of-the-android-application - how to create a splash screen (Mystic Magic, 2013).
 */
package com.example.polyglot;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ibm.cloud.sdk.core.security.Authenticator;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.language_translator.v3.LanguageTranslator;
import com.ibm.watson.language_translator.v3.model.IdentifiableLanguages;
import com.ibm.watson.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.text_to_speech.v1.model.SynthesizeOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible for starting the application.
 * <br>
 * It is used to authorise IBM Watson API account and populate database with available languages.
 * <br>
 * It also loads Text-to-Speech voices onto voiceSetup - a map which is used in Translate class
 * to change the pronounced voices according to the language that is being translated.
 * <br>
 * The activity appears as a loading screen with the app logo and a progress bar which indicates
 * the loading of data.
 * <br>
 * It is intended to avoid the app from crashing if there is no internet connection and the data
 * cannot be initialised. This is handled by the isConnected boolean variable. When isConnected is
 * false, the app continues working, though it is in offline mode and certain features are off:
 * <br> - Subscribed Languages activity will not be populated because the app will not be able to access
 * the internet.
 * <br> - The selection of items in Translate activity, as well as Translate All button and
 * Pronounce button will be switched off. The user can use View All to still use the app to see
 * translations that have been stored on the database.
 * <br> No new translations will be possible without accessing the service, therefore internet is
 * required for this task.
 * <br> Orientation changes are handled manually to allow authorisation and data to be loaded only once.
 * <p>
 * The app is tested on a Pixel2 API 28, but intended for use on smaller/larger screens as well.
 * </p>
 * @author dim6ata
 */
public class MainActivity extends AppCompatActivity {

    protected static DataControl data;
    protected static Map<String, String> langMap;
    protected static Map<String, String> voiceSetup;
    protected static ArrayList<String> langNames;
    protected static LanguageTranslator translationService;
    protected static TextToSpeech textToSpeech;
    protected static boolean isConnected;//value will be false if there is an error connecting to IBM server.
    private boolean isComplete = true;
    private int progress = 0;
    private ProgressBar progressBar;
    private boolean flag = false;
    Thread progressThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        /**
         * sets up the view depending on whether orientation is portrait or landscape.
         * Manual setup required due to android:configChanges="orientation" being added to manifest.
         */
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

            setContentView(R.layout.activity_main);//loads activity with portrait layout

        } else {

            setContentView(R.layout.activity_main_landscape);//loads activity with landscape layout

        }

        progressBar = findViewById(R.id.progressBar);

        if (!flag) {//only runs the first time the activity is started.

            flag = true;
            data = new DataControl(this);
            langMap = new HashMap<>();
            langNames = new ArrayList<>();
            isConnected = true;

            new GetLanguages().execute();

            startProgressBarThread();


        }
    }

    private void startProgressBarThread() {

        progressThread = new Thread() {
            /**
             * creates a thread that updates the progressbar every 50ms.
             * It is intended to guide the user that a loading procedure is in action.
             */
            @Override
            public void run() {
                try {
                    super.run();

                    while (isComplete) {
                        progress++;
                        sleep(50);
                        progressBar.setProgress(progress);
                    }
                } catch (Exception e) {

                }
            }
        };
        progressThread.start();

    }

    /**
     * handles orientation configuration changes.
     *
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);


        if (progressThread.isAlive()) {//interrupts the progress bar thread in case it is alive
            progressThread.interrupt();
        }

        /**
         * performs orientation changes to layout
         */
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

            setContentView(R.layout.activity_main);//sets the view to portrait

        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            setContentView(R.layout.activity_main_landscape);//sets the view to portrait

        }


        /**
         * resets layout fields as setContentView resets all.
         */
        progressBar = findViewById(R.id.progressBar);
        startProgressBarThread();//restarts the thread, continuing from progress values that have been previously incremented.


    }

    /**
     * initialises the translation service by assigning api key and url of IBM Language Translator API.
     *
     * @return provides the activity with the authorised LanguageTranslator service.
     */
    private LanguageTranslator initLanguageTranslatorService() {
        Authenticator authenticator
                = new IamAuthenticator(getString(R.string.language_translator_apikey));
        LanguageTranslator service = new LanguageTranslator("2018-05-01", authenticator);
        service.setServiceUrl(getString(R.string.language_translator_url));
        return service;
    }

    private TextToSpeech initTextToSpeechService() {
        Authenticator authenticator = new IamAuthenticator(getString(R.string.text_speech_apikey));
        TextToSpeech service = new TextToSpeech(authenticator);
        service.setServiceUrl(getString(R.string.text_speech_url));
        return service;
    }

    /**
     * retrieves available languages from IBM Watson Language Translator service.
     * Takes the JSON format and extracts language codes and language names.
     * Adds the individual elements name and language to a static map - langMap, which will be used at other activities.
     * It also adds names to langNames for further ease in querying.
     *
     * @throws JSONException
     */
    public void listLanguages() throws JSONException {

        IdentifiableLanguages languages = translationService.listIdentifiableLanguages().execute().getResult();

        JSONObject langData = new JSONObject(languages.toString());
        JSONArray allLangData = langData.getJSONArray("languages");

        for (int i = 0; i < allLangData.length(); i++) {

            JSONObject subData = allLangData.getJSONObject(i);
            String lang = subData.getString("language");
            String name = subData.getString("name");

            langMap.put(name, lang);//adds elements to map.
            langNames.add(name);//adds name to a list.

        }

        Collections.sort(langNames);//sorts langNames alphabetically.

    }

    /**
     * sets up voices for different languages, where available
     */
    public void setUpVoices() {

        voiceSetup = new HashMap<>();
        voiceSetup.put("en", SynthesizeOptions.Voice.EN_US_ALLISONV3VOICE);
        voiceSetup.put("de", SynthesizeOptions.Voice.DE_DE_BIRGITV3VOICE);
        voiceSetup.put("es", SynthesizeOptions.Voice.ES_ES_LAURAV3VOICE);
        voiceSetup.put("ar", SynthesizeOptions.Voice.AR_AR_OMARVOICE);
        voiceSetup.put("fr", SynthesizeOptions.Voice.FR_FR_RENEEV3VOICE);
        voiceSetup.put("it", SynthesizeOptions.Voice.IT_IT_FRANCESCAVOICE);
        voiceSetup.put("ja", SynthesizeOptions.Voice.JA_JP_EMIVOICE);
        voiceSetup.put("pt", SynthesizeOptions.Voice.PT_BR_ISABELAV3VOICE);
        voiceSetup.put("nl", SynthesizeOptions.Voice.NL_NL_EMMAVOICE);
        voiceSetup.put("zh", SynthesizeOptions.Voice.ZH_CN_LINAVOICE);


    }

    /**
     * inner class that is responsible for authorising and retrieving elements from IBM Watson service.
     * It is used to start a separate thread than the main UI thread to avoid the system from crashing.
     * When finished the Home Activity gets invoked.
     */
    private class GetLanguages extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        /**
         * translationService is used to authorise IBM service
         * listLanguages retrieves available languages on the the IBM service.
         *
         * @param voids
         * @return
         */
        @Override
        protected Boolean doInBackground(Void... voids) {

            boolean flag = false;
            try {
                translationService = initLanguageTranslatorService();//check if this would be needed more than once and if yes, put outside if block.
                textToSpeech = initTextToSpeechService();
                listLanguages();
                setUpVoices();
                flag = true;
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                return flag;
            }


        }

        /**
         * @param bool if the value of bool is false the isConnected is set to false, which means that there has been a problem with connecting to the IBM Watson Service.
         */
        @Override
        protected void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);

            isComplete = false;

            if (!bool) {
                isConnected = false;
                new ToastedToast(MainActivity.this, 0).setToast("There has been a connection error. You can use app in offline mode only.", Toast.LENGTH_LONG);
            }

            Intent intent = new Intent(MainActivity.this, HomeScreen.class);
            startActivity(intent);
            finish();
        }
    }


}
