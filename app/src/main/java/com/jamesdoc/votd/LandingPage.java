package com.jamesdoc.votd;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;


public class LandingPage extends ActionBarActivity {
    TextView votdResponse;
    TextView votdReference;
    TextView votdCopyright;
    TextView data_from;
    ViewGroup app_body;
    Toast loading_toast;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    String votd_url = "https://www.biblegateway.com/votd/get/?format=json&version=";
    String version = "nivuk";
    String call_url = votd_url.concat(version);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        setContentView(R.layout.activity_landing_page);
        // get reference to the views
        app_body = (ViewGroup)findViewById(R.id.main_body);
        votdResponse = (TextView) findViewById(R.id.votdResponse);
        votdReference = (TextView) findViewById(R.id.votdReference);
        votdCopyright = (TextView) findViewById(R.id.votdCopyright);
        data_from = (TextView) findViewById(R.id.data_from);

        getVerse();

        // Build navigation drawer
//        String[] values = new String[] { "One", "Two", "Three" };
//        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//        mDrawerList = (ListView) findViewById(R.id.navList);
//        ArrayAdapter<String> ad = new ArrayAdapter<String>(this, R.layout.nav_drawer_list, values);
//        mDrawerList.setAdapter(ad);

        // Enable links within various strings
        data_from.setMovementMethod(LinkMovementMethod.getInstance());
    }


    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            try{

                JSONObject json_votd = new JSONObject(result);
                json_votd = json_votd.getJSONObject("votd");

                String verse_content = json_votd.getString("content");
                String verse_reference = json_votd.getString("display_ref");
                String version_copyright = json_votd.getString("copyright");

                votdResponse.setText(Html.fromHtml(verse_content));
                votdReference.setText(verse_reference);
                votdCopyright.setText(Html.fromHtml(version_copyright));


            } catch (JSONException e){
                throw new RuntimeException(e);
            }
        }
    }

    public void openSettings(View view) {
        app_body.removeAllViews();
        app_body.addView(View.inflate(this, R.layout.settings_page, null));
        closeDrawer(view);
    }

    public void openMain(View view) {
        app_body.removeAllViews();
        app_body.addView(View.inflate(this, R.layout.main_page, null));

        app_body = (ViewGroup)findViewById(R.id.main_body);
        votdResponse = (TextView) findViewById(R.id.votdResponse);
        votdReference = (TextView) findViewById(R.id.votdReference);
        votdCopyright = (TextView) findViewById(R.id.votdCopyright);
        data_from = (TextView) findViewById(R.id.data_from);

        getVerse();

        closeDrawer(view);
    }

    public void getVerse(){
        // If the app is connected then go fetch some JSON
        if(isConnected()){
            Toast loading_toast = Toast.makeText(getBaseContext(), "Looking up verse...", Toast.LENGTH_SHORT);
            loading_toast.show();
            new HttpAsyncTask().execute(call_url);
        } else {
            Toast.makeText(getBaseContext(), "No internet connection.", Toast.LENGTH_LONG).show();
        }
    }

    public void closeDrawer(View view) {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawers();
    }
}
