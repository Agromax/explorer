package com.rdfex;

import android.app.ActionBar;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rdfex.rdf.Triple;
import com.rdfex.rdf.TripleLoader;
import com.rdfex.util.Constants;

import java.util.ArrayList;

public class ViewerActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setTitle("Triples");
        init();
    }

    private void init() {
        final String term = getIntent().getExtras().getString(Constants.TERM_NAME);

        // Lets search the database
        new AsyncTask<Void, Void, ArrayList<Triple>>() {

            @Override
            protected ArrayList<Triple> doInBackground(Void... params) {
                return TripleLoader.loadTriple(ViewerActivity.this, term);
            }

            @Override
            protected void onPostExecute(ArrayList<Triple> triples) {
                LinearLayout list = (LinearLayout) findViewById(R.id.rdf_list);

                if (triples != null && list != null) {
                    if (triples.size() == 0) {
                        View noTriplesView = getLayoutInflater().inflate(R.layout.triples_not_found, null);
                        list.addView(noTriplesView);
                    } else {

                        for (Triple t : triples) {
                            final View rdfView = getLayoutInflater().inflate(R.layout.rdf_view, null);
                            final TextView subjView = (TextView) rdfView.findViewById(R.id.subject);
                            TextView predView = (TextView) rdfView.findViewById(R.id.predicate);
                            TextView objView = (TextView) rdfView.findViewById(R.id.object);

                            subjView.setText(t.getSubject());
                            predView.setText(t.getPredicate());
                            objView.setText(t.getObject());
                            subjView.setTag(t.getId());

                            Button viewButton = (Button) rdfView.findViewById(R.id.view_rdf_btn);
                            viewButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    handleViewRDF(subjView.getTag());
                                }
                            });
                            list.addView(rdfView);
                        }
                    }
                }
            }
        }.execute();

    }

    private void handleViewRDF(Object tag) {
        if (tag != null) {
            String id = (String) tag;
            System.out.println("RDF ID: " + id);
            Intent expandIntent = new Intent(this, RDFExpandActivity.class);
            expandIntent.putExtra(Constants.RDF_ID, id);
            startActivity(expandIntent);
        }

    }
}
