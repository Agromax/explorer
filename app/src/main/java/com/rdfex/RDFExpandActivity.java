package com.rdfex;

import android.app.ActionBar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.rdfex.util.Constants;
import com.rdfex.util.ExUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

public class RDFExpandActivity extends AppCompatActivity {

    private String tripleId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rdfexpand);

        setupHomeButton();
        init();
    }

    private void init() {
        Bundle extras = getIntent().getExtras();
        this.tripleId = extras.getString(Constants.RDF_ID);

        loadTriple();
    }

    private void loadTriple() {
        if (tripleId != null) {
            new AsyncTask<Void, Void, String>() {

                @Override
                protected String doInBackground(Void... params) {
                    return load();
                }

                @Override
                protected void onPostExecute(String s) {
                    if (s != null && !s.isEmpty()) {
                        try {
                            JSONObject t = new JSONObject(s);
                            int code = t.getInt("code");
                            if (code == 0) {
                                addToUI(t.getJSONObject("msg"));
                            } else {
                                ExUtil.alert(RDFExpandActivity.this, t.getJSONObject("msg").toString());
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        ExUtil.alert(RDFExpandActivity.this, "Something went wrong, exiting");
                        finish();
                    }
                }
            }.execute();

        } else {
            System.out.println("Triple Id is null, this should not have happened at this point");
            ExUtil.alert(this, "Triple Id is null. Inconsistent activity state, exiting.");
            finish();
        }
    }

    private void addToUI(JSONObject triple) {
        String sub = triple.optString("sub", "");
        String obj = triple.optString("obj", "");
        String pre = triple.optString("pre", "");

        TextView subView = (TextView) findViewById(R.id.sub);
        TextView preView = (TextView) findViewById(R.id.pre);
        TextView objView = (TextView) findViewById(R.id.obj);

        if (subView != null && objView != null && preView != null) {
            subView.setText(sub);
            preView.setText(pre);
            objView.setText(obj);
        }
    }

    /**
     * THis method connects to the server and downloads the required triple.
     * This method must be call from an external thread and not from the main UI thread
     *
     * @return triple details as JSON string or null
     */
    private String load() {
        String url = getString(R.string.triple_url) + "?id=" + tripleId;
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = Constants.HTTP_CLIENT.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Adds a home button in the Action bar
     */
    private void setupHomeButton() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
