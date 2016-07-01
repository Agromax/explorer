package com.rdfex;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.Random;

public class TreeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree);
        init();
    }

    private void init() {
        TableLayout table = (TableLayout) findViewById(R.id.table);
        if (table != null) {
            table.removeAllViews();
            Random r = new Random(1L);
            for (int i = 0; i < 50; i++) {
                table.addView(getRow(r.nextInt(16)));
            }
        }

    }


    private TableRow getRow(int gapUnit) {
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);

        Space space = new Space(this);
        ll.addView(space);
        ViewGroup.LayoutParams layoutParams = space.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(60, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        layoutParams.width = 60 * gapUnit;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        space.setLayoutParams(layoutParams);

        Button btn = new Button(this);
        btn.setText("Hello");
        ll.addView(btn);

        TableRow row = new TableRow(this);
        row.addView(ll);

        return row;
    }
}
