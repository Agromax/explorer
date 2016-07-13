package com.rdfex;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.rdfex.util.Constants;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

public class AffixTextActivity extends AppCompatActivity {

    private String tripleId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_affix_text);

        tripleId = getIntent().getExtras().getString("tripleId");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.affix_text_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.affix_close:
                sendAffixedTextAndExit();
                break;
            case R.id.affix_send:
                sendAffixedTextAndExit();
                break;
        }
        return true;
    }

    private void sendAffixedTextAndExit() {
        EditText text = (EditText) findViewById(R.id.affix_text);
        final String s = text.getText().toString();

        if (!s.isEmpty()) {
            final ProgressDialog progressDialog = ProgressDialog.show(this, "", "Posting you knowledge", true);

            new AsyncTask<Void, Void, String>() {

                @Override
                protected String doInBackground(Void... params) {
                    FormBody body = new FormBody.Builder()
                            .add("content", s)
                            .add("user", Constants.ACTIVE_USER.getUserId())
                            .add("token", Constants.ACTIVE_USER.getSessionToken())
                            .add("triple", tripleId)
                            .build();

                    Request request = new Request.Builder()
                            .url(getString(R.string.affix_text_url))
                            .post(body)
                            .build();
                    try {
                        Response response = Constants.HTTP_CLIENT.newCall(request).execute();
                        return response.body().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(String s) {
                    System.out.println(s);
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                }
            }.execute();
        }
    }
}
