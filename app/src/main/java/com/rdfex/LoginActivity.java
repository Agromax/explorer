package com.rdfex;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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

        setTitle("Login");

        Button loginButton = (Button) findViewById(R.id.login_btn);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manageLogin();
            }
        });

        Button registerButton = (Button) findViewById(R.id.al_register);
        if (registerButton != null) {
            registerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleRegisterAction();
                }
            });
        }


        String activeUser = testLoggedIn();
        if (activeUser != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(Constants.LOGIN_RESULT, activeUser);
            setResult(RESULT_OK, resultIntent);
            finish();
        }


    }

    private void handleRegisterAction() {
        EditText name = (EditText) findViewById(R.id.al_real_name);
        EditText email = (EditText) findViewById(R.id.al_user_email);
        EditText password = (EditText) findViewById(R.id.al_user_password);

        String n = name.getText().toString();
        String e = email.getText().toString();
        String p = password.getText().toString();

        registerUser(n, e, p);
    }

    private void registerUser(String name, String email, String pass) {
        final FormBody body = new FormBody.Builder()
                .add("name", name)
                .add("email", email)
                .add("password", pass)
                .build();

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                Request request = new Request.Builder()
                        .url(getString(R.string.register_url))
                        .post(body)
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
                        JSONObject res = new JSONObject(s);
                        int code = res.getInt("code");
                        if (code == 0) {
                            JSONObject user = res.getJSONObject("msg");
                            ExUtil.writeFile(LoginActivity.this, getString(R.string.credential_file_name), user.toString());
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra(Constants.LOGIN_RESULT, user.toString());
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();

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
                        new AlertDialog.Builder(LoginActivity.this)
                                .setCancelable(false)
                                .setMessage(e.getMessage())
                                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                }).create()
                                .show();
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
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage("Parameters Missing")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).create()
                    .show();
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
