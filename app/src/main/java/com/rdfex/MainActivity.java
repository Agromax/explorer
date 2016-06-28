package com.rdfex;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        Button explore = (Button) findViewById(R.id.explore_btn);
        if (explore != null) {
            explore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleExplore();
                }
            });
        }
    }

    /**
     * Starts a new explorer activity
     *
     * @see ExplorerActivity
     */
    private void handleExplore() {
        Intent explorerIntent = new Intent(this, ExplorerActivity.class);
        startActivity(explorerIntent);
    }


}
