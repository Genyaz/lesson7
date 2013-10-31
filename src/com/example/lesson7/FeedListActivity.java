package com.example.lesson7;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.example.lesson7.databases.ChannelsDataBase;
import com.example.lesson7.databases.FeedDataBase;

import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: Genyaz
 * Date: 23.10.13
 * Time: 18:19
 * To change this template use File | Settings | File Templates.
 */
public class FeedListActivity extends Activity {
    private ListView feedList;
    private FeedAdapter feedAdapter;
    private Context context;
    private int channel_id;

    public class FeedAdapter extends BaseAdapter {

        private class FeedView {
            Feed feed;
            TextView view;

            public FeedView(Feed feed) {
                this.feed = feed;
                this.view = new TextView(context);
                this.view.setTextSize(20);
                this.view.setText(feed.title);
            }
        }

        private Vector<FeedView> feedViews;

        public FeedAdapter() {
            feedViews = new Vector<FeedView>();
        }

        public void addFeed(Feed element) {
            feedViews.add(new FeedView(element));
            feedAdapter.notifyDataSetChanged();
        }

        public String getDescription(int position) {
            return feedViews.get(position).feed.description;
        }

        @Override
        public int getCount() {
            return feedViews.size();
        }

        @Override
        public Object getItem(int position) {
            return feedViews.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return feedViews.get(position).view;
        }
    }

    public void onFeedClick(int position) {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("description", feedAdapter.getDescription(position));
        startActivity(intent);
    }

    public void downloadFeed(int channel_id) {
        FeedDataBase fdb = new FeedDataBase(this);
        SQLiteDatabase rdb = fdb.getReadableDatabase();
        Cursor cursor = rdb.query(FeedDataBase.DATABASE_NAME, null, FeedDataBase.CHANNEL_ID + "='" + channel_id +"'",
                null, null, null, null, "100");
        int title_id = cursor.getColumnIndex(FeedDataBase.TITLE);
        int description_id = cursor.getColumnIndex(FeedDataBase.DESCRIPTION);
        while (cursor.moveToNext()) {
            feedAdapter.addFeed(new Feed(
                    cursor.getString(title_id),
                    cursor.getString(description_id)));
        }
        cursor.close();
        rdb.close();
        fdb.close();
    }

    public void onRemoveChannelClick(View view) {
        ChannelsDataBase cdb = new ChannelsDataBase(this);
        SQLiteDatabase wdb = cdb.getWritableDatabase();
        wdb.delete(ChannelsDataBase.DATABASE_NAME,
                ChannelsDataBase._ID + " = " + channel_id, null);
        wdb.close();
        cdb.close();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed);
        context = this;
        feedAdapter = new FeedAdapter();
        feedList = (ListView) findViewById(R.id.feedList);
        feedList.setAdapter(feedAdapter);
        feedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onFeedClick(position);
            }
        });
        Intent intent = getIntent();
        channel_id = intent.getIntExtra("channel_id", 0);
        downloadFeed(channel_id);
    }
}