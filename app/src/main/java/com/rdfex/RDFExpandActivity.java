package com.rdfex;

import android.app.ActionBar;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rdfex.util.Constants;
import com.rdfex.util.ExUtil;

import org.json.JSONArray;
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

        setTitle("Expanded View");
        setupHomeButton();
        init();
    }

    private void init() {
        Bundle extras = getIntent().getExtras();
        this.tripleId = extras.getString(Constants.RDF_ID);

        Button addText = (Button) findViewById(R.id.add_text);
        if (addText != null) {
            addText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleAddText();
                }
            });
        }

        loadTriple();
        loadAffixedKnowledge();
    }

    private void loadAffixedKnowledge() {
        final LinearLayout extraKnowledgeList = (LinearLayout) findViewById(R.id.extra_info_list);
        extraKnowledgeList.removeAllViews();
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                String url = getString(R.string.get_affixed_text) + "?id=" + tripleId;

                Request request = new Request.Builder()
                        .url(url)
                        .build();

                try {
                    Response res = Constants.HTTP_CLIENT.newCall(request).execute();
                    return res.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                if (s != null) {
                    try {
                        JSONObject ts = new JSONObject(s);
                        int code = ts.getInt("code");
                        if (code == 0) {
                            JSONArray msgs = ts.getJSONArray("msg");
                            for (int i = 0; i < msgs.length(); i++) {
                                JSONObject kw = msgs.getJSONObject(i);
                                String ctype = kw.getString("contentType");
                                if (ctype.equalsIgnoreCase("text")) {
                                    addTextKnowledge(kw);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();
    }

    private void addTextKnowledge(final JSONObject kw) {
        final LinearLayout extraKnowledgeList = (LinearLayout) findViewById(R.id.extra_info_list);

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                try {
                    String userId = kw.getString("userId");
                    Request request = new Request.Builder()
                            .url(getString(R.string.user_info_url) + "?id=" + userId)
                            .build();

                    Response res = Constants.HTTP_CLIENT.newCall(request).execute();
                    return res.body().string();
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                if (s != null) {
                    System.out.println(s);
                    try {
                        JSONObject uu = new JSONObject(s);
                        JSONObject u = uu.getJSONObject("msg");
                        String name = u.getString("name");
                        String content = kw.getString("content");

                        View view = getLayoutInflater().inflate(R.layout.affixed_knowledge_layout, null);
                        TextView userName = (TextView) view.findViewById(R.id.user_name);
                        TextView text = (TextView) view.findViewById(R.id.text_content);

                        userName.setText(name);
                        text.setText(content);

                        extraKnowledgeList.addView(view);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();
    }

    private void handleAddText() {
        Intent intent = new Intent(this, AffixTextActivity.class);
        intent.putExtra("tripleId", tripleId);
        startActivity(intent);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.expanded_view_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.evm_sync:
                syncKnowledge();
                break;
        }
        return true;
    }

    private void syncKnowledge() {
        loadAffixedKnowledge();
    }

}
