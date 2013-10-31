package com.example.lesson7.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created with IntelliJ IDEA.
 * User: Genyaz
 * Date: 31.10.13
 * Time: 18:48
 * To change this template use File | Settings | File Templates.
 */
public class ChannelsDataBase extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;

    public static final String _ID = "_id";
    public static final String DATABASE_NAME = "channelsdb";
    public static final String CHANNEL_NAME = "name";
    public static final String CHANNEL_URL = "url";

    public static final String CREATE_DATABASE = "CREATE TABLE " + DATABASE_NAME
            + " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + CHANNEL_NAME + " TEXT, " + CHANNEL_URL + " TEXT);";

    public static final String DROP_DATABASE = "DROP TABLE IF EXISTS " + DATABASE_NAME;

    public ChannelsDataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DATABASE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion != oldVersion) {
            db.execSQL(DROP_DATABASE);
            onCreate(db);
        }
    }
}