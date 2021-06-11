package com.example.polyglot;

import android.provider.BaseColumns;

/**
 * Interface that holds database table names and columns information
 * @author dim6ata
 */
public interface TableSetup extends BaseColumns {


    /**
     * Tables in database:
     */
    public static final String TABLE1_NAME = " Phrases ";
    public static final String TABLE2_NAME = " Languages ";
    public static final String TABLE3_NAME = " LanguageTranslations ";


    /**
     * Columns in the Events database:
     */
    public static final String TABLE1_COL1 = "phrase";
    public static final String TABLE2_COL1 = "language";
    public static final String TABLE2_COL2 = "name";
    public static final String TABLE3_COL1 = "translatedPhrase";
    public static final String TABLE3_COL2 = "language";
    public static final String TABLE3_COL3 = "phrase";

}
