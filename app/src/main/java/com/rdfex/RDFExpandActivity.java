package com.rdfex;

import android.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class RDFExpandActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rdfexpand);

        setupHomeButton();

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
