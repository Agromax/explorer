package com.rdfex;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.rdfex.util.Constants;
import com.rdfex.util.ExUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button loginButton = (Button) findViewById(R.id.login_btn);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manageLogin();
            }
        });


        String activeUser = testLoggedIn();
        if (activeUser != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(Constants.LOGIN_RESULT, activeUser);
            setResult(RESULT_OK, resultIntent);
            finish();
        }


    }

    private void manageLogin() {

        final HashMap<String, String> logInInputs = getLogInInputs();
        if (logInInputs.size() >= 2) {


            new AsyncTask<Void, Void, String>() {

                @Override
                protected String doInBackground(Void... params) {
                    RequestBody body = new FormBody.Builder()
                            .add("email", logInInputs.get("email"))
                            .add("password", logInInputs.get("password")).build();

                    Request login = new Request.Builder()
                            .url(getString(R.string.login_url))
                            .post(body)
                            .build();

                    try {
                        Response result = client.newCall(login).execute();
                        return result.body().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(String s) {
                    try {
                        JSONObject user = new JSONObject(s);
                        int code = user.getInt("code");
                        if (code == 0) {
                            JSONObject u = user.getJSONObject("msg");
                            ExUtil.writeFile(LoginActivity.this, getString(R.string.credential_file_name), u.toString());
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra(Constants.LOGIN_RESULT, u.toString());
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else {
                            ExUtil.alert(LoginActivity.this, user.toString());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        ExUtil.alert(LoginActivity.this, e.getMessage());
                    }

                }
            }.execute();

        } else {
            // Show dialog indicating that some data is missing
            ExUtil.alert(this, "Parameters Missing");
        }
    }

    private HashMap<String, String> getLogInInputs() {
        EditText email = (EditText) findViewById(R.id.email);
        EditText password = (EditText) findViewById(R.id.password);

        HashMap<String, String> inputs = new HashMap<>();

        if (email != null) {
            inputs.put("email", email.getText().toString());
        }

        if (password != null) {
            inputs.put("password", password.getText().toString());
        }
        return inputs;
    }

    private String testLoggedIn() {
        String content = ExUtil.readFile(this, getString(R.string.credential_file_name));
        if (content.isEmpty()) {
            return null;
        }
        try {
            JSONObject user = new JSONObject(content);
            /*String name = user.optString("name", null);
            String email = user.optString("email", null);
            String userId = user.optString("userId", null);
            String token = user.optString("sessionToken", null);
*/
            return content;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
