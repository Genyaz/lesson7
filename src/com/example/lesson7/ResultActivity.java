package com.example.lesson7;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * Created with IntelliJ IDEA.
 * User: Genyaz
 * Date: 23.10.13
 * Time: 18:19
 * To change this template use File | Settings | File Templates.
 */
public class ResultActivity extends Activity {
    private WebView webView;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);
        webView = (WebView) findViewById(R.id.feedView);
        Intent intent = getIntent();
        String description = intent.getStringExtra("description");
        webView.loadDataWithBaseURL(null, description, "text/html", "UTF-8", null);
    }
}