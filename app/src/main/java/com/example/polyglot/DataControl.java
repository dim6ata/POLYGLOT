package com.example.polyglot;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.provider.BaseColumns._ID;
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
 * Class that handles database manipulation.
 * It creates the database and has methods for adding, deleting, querying and updating.
 * @author dim6ata
 */
public class DataControl extends SQLiteOpenHelper {

    private static final String DB_NAME = "lang.db";
    private static final int DB_VERSION = 1;

    /**
     * Constructor
     *
     * @param context
     */
    public DataControl(Context context) {

        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * runs the first time the database is created or whenever consequently called.
     *
     * @param db receives an sqlite database as parameter.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(" CREATE TABLE" + TABLE1_NAME + "("//table phrases
                + _ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT , "
                + TABLE1_COL1 + " TEXT NOT NULL ); ");

        db.execSQL(" CREATE TABLE" + TABLE2_NAME + "( "//table language
                + _ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT , "
                + TABLE2_COL1 + " TEXT NOT NULL, " + TABLE2_COL2 + " TEXT NOT NULL ); ");//1 = language code; 2 = language name.

        db.execSQL(" CREATE TABLE" + TABLE3_NAME + "("//table translations
                + _ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT , "
                + TABLE3_COL1 + " TEXT NOT NULL, " //1 = translated phrase;
                + TABLE3_COL2 + " TEXT NOT NULL, "//2 = language code;
                + TABLE3_COL3 + " TEXT NOT NULL, "//3 = phrase.
                + "FOREIGN KEY(" + TABLE3_COL2 + ") REFERENCES"
                + TABLE2_NAME + "(" + TABLE2_COL1 + ") ON DELETE CASCADE ON UPDATE CASCADE, "
                + "FOREIGN KEY(" + TABLE3_COL3 + ") REFERENCES"
                + TABLE1_NAME + "(" + TABLE1_COL1 + ") ON DELETE CASCADE ON UPDATE CASCADE);");

        db.execSQL("PRAGMA foreign_keys = ON;");


    }

    /**
     * used for whenever the database gets upgraded.
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(" DROP TABLE IF EXISTS " + TABLE1_NAME);
        onCreate(db);

    }

    /**
     * adds data to the database
     *
     * @param table  name of table that data would be added to.
     * @param column name of column that data would be added to.
     * @param text   value that would be added to the given column and table.
     * @return returns true if the adding of data is successful, otherwise returns false.
     */
    public boolean addData(String table, String column, String text) {

        boolean flag = false;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(column, text);
            db.insertOrThrow(table, null, values);
            flag = true;
            db.close();

        } finally {
            return flag;
        }

    }

    /**
     * adds data to the database to two columns per query simultaneously.
     *
     * @param table   name of table that data would be added to.
     * @param column1 name of first column that data would be added to.
     * @param text1   value that would be added to the given column and table.
     * @param column2 name of second column that data would be added to
     * @param text2   value that would be added to the given column and table.
     */
    public boolean addData(String table, String column1, String text1, String column2, String text2) {

        boolean flag = false;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(column1, text1);
            values.put(column2, text2);
            db.insertOrThrow(table, null, values);
            flag = true;
            db.close();
        } finally {
            return flag;
        }
    }

    public boolean addData(String table, String column1, String column2, String column3, String text1, String text2, String text3) {

        boolean flag = false;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(column1, text1);
            values.put(column2, text2);
            values.put(column3, text3);
            db.insertOrThrow(table, null, values);
            flag = true;
            db.close();
        } finally {
            return flag;
        }
    }

    /**
     * retrieves all elements from database according to preset cursor.
     *
     * @param cursor position depending on query at getData() method.
     * @param column column that data would be retrieved from.
     * @return - returns an array list with all the elements that have been queried.
     */
    public ArrayList<String> viewData(Cursor cursor, String column) {

        ArrayList<String> list = new ArrayList<>();

        while (cursor.moveToNext()) {

            String phraseId = cursor.getString(cursor.getColumnIndexOrThrow(column));
            list.add(phraseId);

        }
        cursor.close();

        return list;
    }

    /**
     * retrieves all elements from database according to preset cursor.
     *
     * @param cursor  position depending on query at getData() method.
     * @param column1 first column that a phrase would be retrieved from.
     * @param column2 column that id would be retrieved from.
     * @return returns a map with phrases as keys and id-s as values.
     */
    public Map<String, String> viewData(Cursor cursor, String column1, String column2) {

        Map<String, String> map = new HashMap<>();

        while (cursor.moveToNext()) {

            String phrase = cursor.getString(cursor.getColumnIndexOrThrow(column1));
            String phraseId = cursor.getString(cursor.getColumnIndexOrThrow(column2));
            map.put(phrase, phraseId);

        }
        cursor.close();

        return map;
    }


    /**
     * retrieves a cursor depending on elements queried. To be used by viewData() method.
     *
     * @param table     holds the table name.
     * @param values    the elements to be selected in a string [].
     * @param selection where clause.
     * @param order     order by string which contains the requirement for organising the data.
     * @param selArgs   arguments that follow the where clause
     * @return
     */
    public Cursor getData(String table, String[] values, String selection, String order, String[] selArgs) {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(table, values, selection,
                selArgs, null, null, order);

        return cursor;

    }

    /**
     * updates table data.
     *
     * @param table   table name.
     * @param column  column name.
     * @param textNew the text that needs to be changed.
     * @param textOld the previous text that will be changed. It is used to replace the value.
     * @return - returns true if update is successful, otherwise returns false.
     */
    public boolean updateData(String table, String column, String textNew, String textOld) {

        boolean flag = false;

        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(column, textNew);
            String selection = column + " = ? ";
            String[] selectionArgs = {textOld};
            db.update(table, values, selection, selectionArgs);
            flag = true;
            db.close();

        } finally {
            return flag;
        }

    }

    /**
     * deletes a specific element from database.
     *
     * @param table    value for table to be queried.
     * @param column   value for column to be queried.
     * @param toDelete value of element to be removed.
     * @return returns true if deletion has been successful, otherwise false.
     */
    public boolean deleteData(String table, String column, String toDelete) {
        boolean flag = false;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String selection = column + " = ? ";
            String[] selectionArgs = {toDelete};
            db.delete(table, selection, selectionArgs);
            flag = true;
            db.close();
        } finally {

            return flag;
        }
    }

    /**
     * deletes all elements from a table.
     *
     * @param table value for table to be queried.
     * @return returns true if deletion has been successful, otherwise false.
     */
    public boolean deleteAll(String table) {

        boolean flag = false;

        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("DELETE FROM " + table);
            flag = true;
            db.close();
        } finally {
            return flag;
        }

    }

}
