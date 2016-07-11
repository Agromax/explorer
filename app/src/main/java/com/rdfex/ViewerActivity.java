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
        init();
    }

    private void init() {
        final String term = getIntent().getExtras().getString(Constants.TERM_NAME);

        // Lets search the database
        new AsyncTask<Void, Void, ArrayList<Triple>>() {

            @Override
            protected ArrayList<Triple> doInBackground(Void... params) {
                return TripleLoader.loadTriple(term);
            }

            @Override
            protected void onPostExecute(ArrayList<Triple> triples) {
                LinearLayout list = (LinearLayout) findViewById(R.id.rdf_list);

                if (triples != null && list != null) {

                    for (Triple t : triples) {
                        View rdfView = getLayoutInflater().inflate(R.layout.rdf_view, null);
                        TextView subjView = (TextView) rdfView.findViewById(R.id.subject);
                        TextView predView = (TextView) rdfView.findViewById(R.id.predicate);
                        TextView objView = (TextView) rdfView.findViewById(R.id.object);

                        subjView.setText(t.getSubject());
                        predView.setText(t.getPredicate());
                        objView.setText(t.getObject());

                        Button viewButton = (Button) rdfView.findViewById(R.id.view_rdf_btn);
                        viewButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                handleViewRDF();
                            }
                        });
                        list.addView(rdfView);
                    }
                }
            }
        }.execute();

    }

    private void handleViewRDF() {
        Intent expandIntent = new Intent(this, RDFExpandActivity.class);
        expandIntent.putExtra(Constants.RDF_ID, "fake-id-obviously");
        startActivity(expandIntent);
    }
}
