package com.example.lesson7;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.lesson7.databases.ChannelsDataBase;
import com.example.lesson7.databases.FeedDataBase;
import com.example.lesson7.services.FeedService;

import java.util.Vector;

public class ChannelsListActivity extends Activity {
    /**
     * Called when the activity is first created.
     */

    private ListView channelsList;
    private Intent startServiceIntent;
    private EditText newChannel;
    private EditText deleteChannel;
    private ChannelAdapter channelAdapter;
    private Context context;
    private ChannelsDataBase cdb;
    private SQLiteDatabase wdb;

    public class ChannelAdapter extends BaseAdapter {

        private Vector<RSSChannel> rssChannels;

        public class RSSChannel {
            public View view;
            public String url;
            public int id;

            public RSSChannel(String url, int id) {
                TextView textView = new TextView(context);
                textView.setText(url);
                textView.setTextSize(20);
                view = textView;
                this.url = url;
                this.id = id;
            }
        }

        public ChannelAdapter() {
            rssChannels = new Vector<RSSChannel>();
        }

        public void addChannel(String channel_url, int channel_id) {
            rssChannels.add(new RSSChannel(channel_url, channel_id));
        }

        public int removeChannelByName(String url) {
            RSSChannel channel;
            for (int i = 0; i < rssChannels.size(); i++) {
                channel = rssChannels.get(i);
                if (channel.url.equals(url)) {
                    int id = channel.id;
                    rssChannels.remove(channel);
                    notifyDataSetChanged();
                    return id;
                }
            }
            return -1;
        }

        public int getChannelId(int position) {
            return rssChannels.get(position).id;
        }

        @Override
        public int getCount() {
            return rssChannels.size();
        }

        @Override
        public Object getItem(int position) {
            return rssChannels.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return rssChannels.get(position).view;
        }
    }

    public void onUpdateClick(View view) {
        wdb.close();
        cdb.close();
        startService(startServiceIntent);
        //pseudoIntent();
    }

    public void pseudoIntent() {
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

    public void onAddChannelClick(View view) {
        cdb = new ChannelsDataBase(this);
        wdb = cdb.getWritableDatabase();
        String url = newChannel.getText().toString();
        ContentValues cv = new ContentValues();
        cv.put(ChannelsDataBase.CHANNEL_NAME, url);
        cv.put(ChannelsDataBase.CHANNEL_URL, url);
        int id = (int)wdb.insert(ChannelsDataBase.DATABASE_NAME, null, cv);
        channelAdapter.addChannel(url, id);
        channelAdapter.notifyDataSetChanged();
    }

    public void onDeleteChannelClick(View view) {
        cdb = new ChannelsDataBase(this);
        wdb = cdb.getWritableDatabase();
        String channel_url = deleteChannel.getText().toString();
        int channel_id = channelAdapter.removeChannelByName(channel_url);
        wdb.delete(ChannelsDataBase.DATABASE_NAME,
                ChannelsDataBase._ID + " = " + channel_id, null);

    }

    private void onChannelClick(int position) {
        Intent intent = new Intent(this, FeedListActivity.class);
        intent.putExtra("channel_id", channelAdapter.getChannelId(position));
        startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channels);
        context = this;
        channelAdapter = new ChannelAdapter();
        cdb = new ChannelsDataBase(this);
        wdb = cdb.getWritableDatabase();
        Cursor current = wdb.query(ChannelsDataBase.DATABASE_NAME,
                null, null, null, null, null, null, "100");
        int channel_id = current.getColumnIndex(ChannelsDataBase._ID);
        int channel_name = current.getColumnIndex(ChannelsDataBase.CHANNEL_NAME);
        String name;
        int id;
        while (current.moveToNext()){
            name = current.getString(channel_name);
            id = current.getInt(channel_id);
            channelAdapter.addChannel(name, id);
        }
        current.close();
        channelsList = (ListView) findViewById(R.id.channelList);
        channelsList.setAdapter(channelAdapter);
        channelsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onChannelClick(position);
            }
        });
        newChannel = (EditText) findViewById(R.id.editAdd);
        deleteChannel = (EditText) findViewById(R.id.editDelete);
        startServiceIntent = new Intent(this, FeedService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, startServiceIntent, 0);
        AlarmManager manager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, 1200000, pendingIntent);
    }
}