package com.example.lesson7;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: Genyaz
 * Date: 24.10.13
 * Time: 19:28
 * To change this template use File | Settings | File Templates.
 */
public class RSSDownloader {

    private static class FeedSaxHandler extends DefaultHandler {
        Vector<Feed> feed = new Vector<Feed>();
        boolean inside_entry_or_item = false;
        boolean inside_title = false;
        boolean inside_description = false;
        boolean inside_date = false;
        String encoding;
        StringBuilder description = new StringBuilder();
        StringBuilder title = new StringBuilder();
        StringBuilder date = new StringBuilder();

        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            if (qName.equalsIgnoreCase("ENTRY") || qName.equalsIgnoreCase("ITEM")) {
                inside_entry_or_item = true;
            } else if (inside_entry_or_item) {
                if (qName.equalsIgnoreCase("TITLE")) {
                    inside_title = true;
                } else if (qName.equalsIgnoreCase("SUMMARY") || qName.equalsIgnoreCase("DESCRIPTION")) {
                    inside_description = true;
                } else if (qName.equalsIgnoreCase("PUBDATE") || qName.equalsIgnoreCase("PUBLISHED")) {
                    inside_date = true;
                }
            }
        }

        @Override
        public void endElement(String uri, String localName,
                               String qName) throws SAXException {
            if (qName.equalsIgnoreCase("ENTRY") || (qName.equalsIgnoreCase("ITEM"))) {
                inside_entry_or_item = false;
                if (title.length() > 0) {
                    if (date.length() > 0) {
                        title.append("\n");
                        title.append(date.toString());
                    }
                    feed.add(new Feed(title.toString(), description.toString()));
                    title = new StringBuilder();
                    description = new StringBuilder();
                    date = new StringBuilder();
                }
            } else if (inside_entry_or_item) {
                if (qName.equalsIgnoreCase("TITLE")) {
                    inside_title = false;
                } else if (qName.equalsIgnoreCase("SUMMARY") || qName.equalsIgnoreCase("DESCRIPTION")) {
                    inside_description = false;
                } else if (qName.equalsIgnoreCase("PUBDATE") || qName.equalsIgnoreCase("PUBLISHED")) {
                    inside_date = false;
                }
            }
        }

        @Override
        public void characters(char ch[], int start, int length) throws SAXException {
            if (inside_entry_or_item) {
                if (inside_title) {
                    title.append(ch, start, length);
                } else if (inside_date) {
                    date.append(ch, start, length);
                } else if (inside_description) {
                    description.append(ch, start, length);
                }
            }
        }
    }

    public static final int CHECK_ENCODING_LENGTH = 200;

    public static Vector<Feed> downloadFromURL(String url) {
        Vector<Feed> result = new Vector<Feed>();
        String encoding;
        InputStream ist = null;
        try {
            URLConnection connection = new URL(url).openConnection();
            ist = connection.getInputStream();
            byte[] buffer = new byte[CHECK_ENCODING_LENGTH];
            int really_read = ist.read(buffer, 0, CHECK_ENCODING_LENGTH);
            String enc = new String(buffer, 0, really_read);
            StringBuilder sb = new StringBuilder();
            enc = enc.split("encoding")[1];
            encoding = enc.split("\"")[1];
        } catch (Exception e) {
            encoding = "utf-8";
            if (ist != null) {
                try {
                    ist.close();
                } catch (IOException e1) {
                }
            }
        }
        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            FeedSaxHandler handler = new FeedSaxHandler();
            handler.encoding = encoding;
            HttpGet getRequest = new HttpGet(url);
            HttpClient client = new DefaultHttpClient();
            HttpResponse httpResponse = client.execute(getRequest);
            try {
                InputStreamReader reader = new InputStreamReader(httpResponse.getEntity().getContent(), encoding);
                InputSource is = new InputSource(reader);
                is.setEncoding(encoding);
                saxParser.parse(is, handler);
                result = handler.feed;
            } catch (Exception e) {
                client.getConnectionManager().shutdown();
            }
        } catch (Exception e) {
        }
        return result;
    }
}