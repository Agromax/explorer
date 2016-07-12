package com.rdfex;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.rdfex.util.Constants;
import com.rdfex.util.ExUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private final OkHttpClient client = new OkHttpClient();
    private ActiveUser activeUser = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        testLogIn();
        Button explore = (Button) findViewById(R.id.explore_btn);
        if (explore != null) {
            explore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleExplore();
                }
            });
        }

        Button about = (Button) findViewById(R.id.about_btn);
        if (about != null) {
            about.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(MainActivity.this, AboutActivity.class);
                    startActivity(i);
                }
            });

        }

        Button update = (Button) findViewById(R.id.update_btn);
        if (update != null) {
            update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateVocabulary();
                }
            });
        }
    }

    private void testLogIn() {
        Intent loginActivity = new Intent(this, LoginActivity.class);
        startActivityForResult(loginActivity, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String result = data.getStringExtra(Constants.LOGIN_RESULT);
                ExUtil.alert(this, "Login Successful");
                try {
                    JSONObject user = new JSONObject(result);
                    String name = user.optString("name", null);
                    String email = user.optString("email", null);
                    String userId = user.optString("userId", null);
                    String token = user.optString("sessionToken", null);

                    activeUser = new ActiveUser(name, email, userId, token);
                    Constants.ACTIVE_USER = activeUser;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Updates the vocabulary
     */
    private void updateVocabulary() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Loading the vocabulary");
        dialog.show();

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                // Test if vocab file is present
                if (ExUtil.isVocabularyPresent(MainActivity.this)) {
                    return ExUtil.readFile(MainActivity.this, getString(R.string.vocab_file_name));
                }

                // File not present, lets download it
                String vocabUrl = getString(R.string.vocab_url);
                Request request = new Request.Builder().url(vocabUrl).build();
                try {
                    Response res = client.newCall(request).execute();
                    String vocabContent = res.body().string();
                    ExUtil.writeFile(MainActivity.this, getString(R.string.vocab_file_name), vocabContent);
                    return vocabContent;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String res) {
                dialog.dismiss();

                if (res == null) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Vocabulary could not be loaded. Please try again")
                            .setCancelable(false)
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).create()
                            .show();
                } else {
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Vocabulary loaded successfully!")
                            .setCancelable(false)
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).create()
                            .show();
                }
            }
        }.execute();
    }

    /**
     * Starts a new explorer activity
     *
     * @see ExplorerActivity
     */
    private void handleExplore() {
        Intent treeIntent = new Intent(this, TreeActivity.class);
        startActivity(treeIntent);
    }


}
