package com.rdfex;

import android.app.ActionBar;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rdfex.rdf.Triple;
import com.rdfex.rdf.TripleLoader;

import java.util.ArrayList;

public class LinkedSearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linked_search);

        decorateActionBar();
        setup();
    }

    private void setup() {
        ImageButton fromBtn = (ImageButton) findViewById(R.id.trv_search_from);
        ImageButton toBtn = (ImageButton) findViewById(R.id.trv_search_to);

        if (fromBtn != null) {
            fromBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleSearch();
                }
            });
        }

        if (toBtn != null) {
            toBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleSearch();
                }
            });
        }


    }

    private void handleSearch() {
        final EditText from = (EditText) findViewById(R.id.trv_from);
        final EditText to = (EditText) findViewById(R.id.trv_to);

        String fromStr = "";
        String toStr = "";

        if (from != null) {
            fromStr = from.getText().toString();
        }

        if (to != null) {
            toStr = to.getText().toString();
        }

        // Perform the actual search
        doSearch(fromStr.trim(), toStr.trim());
    }

    private void doSearch(String fromStr, String toStr) {
        if (fromStr.isEmpty() && !toStr.isEmpty()) {
            searchSingle(toStr);
        } else if (!fromStr.isEmpty() && toStr.isEmpty()) {
            searchSingle(fromStr);
        } else if (!fromStr.isEmpty() && !toStr.isEmpty()) {
            connect(fromStr, toStr);
        }
    }

    private void connect(String fromStr, String toStr) {

    }

    private void searchSingle(final String q) {
        System.out.println("Searching for:"+q);
        new AsyncTask<Void, Void, ArrayList<Triple>>() {

            @Override
            protected ArrayList<Triple> doInBackground(Void... params) {
                return TripleLoader.loadTriple(LinkedSearchActivity.this, q);
            }

            @Override
            protected void onPostExecute(ArrayList<Triple> triples) {
                if (triples != null) {
                    LinearLayout results = (LinearLayout) findViewById(R.id.als_search_results);

                    if (results != null) {
                        for (Triple t : triples) {
                            View view = getLayoutInflater().inflate(R.layout.triple_result_view, null);
                            TextView sub = (TextView) view.findViewById(R.id.trv_sub);
                            TextView pre = (TextView) view.findViewById(R.id.trv_pre);
                            TextView obj = (TextView) view.findViewById(R.id.trv_obj);

                            sub.setText(t.getSubject());
                            pre.setText(t.getPredicate());
                            obj.setText(t.getObject());

                            results.addView(view);
                        }
                    }
                }
            }
        }.execute();

    }

    private void decorateActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
        }

        setTitle("Connect Triples");

    }
}
