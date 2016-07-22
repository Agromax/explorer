package com.rdfex;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;

import com.rdfex.util.Constants;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

public class SearchTermActivity extends AppCompatActivity {

    private String[] terms = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_term);

        setTitle("Search Vocabulary");

        setupAutoComplete();
    }


    private void setupAutoComplete() {

        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "Loading terms", true);

        //Step 1 Download the vocabulary
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                Request req = new Request.Builder()
                        .url(getString(R.string.terms_url))
                        .build();

                try {
                    Response res = Constants.HTTP_CLIENT.newCall(req).execute();
                    String vocab = res.body().string();
                    terms = vocab.split("[\r\n]+");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(SearchTermActivity.this,
                        android.R.layout.simple_dropdown_item_1line, terms);

                final AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.ast_query);
                textView.setAdapter(adapter);

                ImageButton search = (ImageButton) findViewById(R.id.ast_search_btn);
                search.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String q = textView.getText().toString();
                        System.out.println(q);
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("queryTerm", q);
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    }
                });
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }


            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search_term_menu, menu);

        MenuItem exit = menu.findItem(R.id.stm_exit);
        exit.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                finish();
                return false;
            }
        });
        return true;
    }
}
