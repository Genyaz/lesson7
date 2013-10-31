package com.example.lesson7.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.lesson7.Feed;
import com.example.lesson7.RSSDownloader;
import com.example.lesson7.databases.ChannelsDataBase;
import com.example.lesson7.databases.FeedDataBase;

import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: Genyaz
 * Date: 24.10.13
 * Time: 18:57
 * To change this template use File | Settings | File Templates.
 */
public class FeedService extends IntentService {

    public FeedService(String name) {
        super(name);
    }

    public FeedService() {
        super("default_name_");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ChannelsDataBase cdb = new ChannelsDataBase(this);
        SQLiteDatabase rdb = cdb.getReadableDatabase();
        FeedDataBase fdb = new FeedDataBase(this);
        SQLiteDatabase wdb = fdb.getWritableDatabase();
        wdb.execSQL(FeedDataBase.DROP_DATABASE);
        wdb.execSQL(FeedDataBase.CREATE_DATABASE);
        Cursor cursor = rdb.query(ChannelsDataBase.DATABASE_NAME,
                null, null, null, null, null, null);
        int url_column = cursor.getColumnIndex(ChannelsDataBase.CHANNEL_URL);
        int id_column = cursor.getColumnIndex(ChannelsDataBase._ID);
        String url;
        int id;
        while (cursor.moveToNext()) {
            url = cursor.getString(url_column);
            id = cursor.getInt(id_column);
            Vector<Feed> answer = RSSDownloader.downloadFromURL(url);
            Feed feed;
            for (int i = 0; i < answer.size(); i++) {
                feed = answer.get(i);
                ContentValues cv = new ContentValues();
                cv.put(FeedDataBase.CHANNEL_ID, id);
                cv.put(FeedDataBase.TITLE, feed.title);
                cv.put(FeedDataBase.DESCRIPTION, feed.description);
                wdb.insert(FeedDataBase.DATABASE_NAME, null, cv);
            }
        }
        cursor.close();
        wdb.close();
        fdb.close();
        rdb.close();
        cdb.close();
    }
}