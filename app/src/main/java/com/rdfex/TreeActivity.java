package com.rdfex;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TableLayout;

import com.rdfex.util.Constants;
import com.rdfex.util.ExUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashSet;

public class TreeActivity extends AppCompatActivity {

    private String vocabText = null;
    private JSONObject vocabJson = null;
    private TableLayout tableLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree);

        tableLayout = (TableLayout) findViewById(R.id.table);
        if (tableLayout != null)
            tableLayout.removeAllViews();

        initiate();
    }


    private void parseVocabulary() {
        if (vocabText != null) {
            try {
                vocabJson = new JSONObject(vocabText);
                addToView(vocabJson, 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void addToView(JSONObject root, int tabIndex) {
        int width = computeWidth(tabIndex);

        LinearLayout ll = new LinearLayout(this);
        addSpace(ll, width);
        addContent(ll, root);
        tableLayout.addView(ll);

        // Should we expand this node ?
        boolean expand = root.optBoolean("expand", false);

        if (expand || tabIndex == 0) {
            JSONArray children = null;
            try {
                children = root.getJSONArray("nodes");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (children != null) {
                for (int i = 0; i < children.length(); i++) {
                    try {
                        JSONObject child = children.getJSONObject(i);
                        addToView(child, tabIndex + 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private int computeWidth(int index) {
        return Constants.GAP_WIDTH_QUANTA * index;
    }

    private void addSpace(ViewGroup parent, int width) {
        Space space = new Space(this);
        parent.addView(space);

        ViewGroup.LayoutParams layoutParams = space.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        space.setLayoutParams(layoutParams);
    }

    private void addContent(ViewGroup parent, final JSONObject node) {
        String term = node.optString("text", null);
        if (term != null) {
            Button button = new Button(this);
            button.setText(term);
            button.setAllCaps(false);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean expand = node.optBoolean("expand", false);
                    try {
                        node.remove("expand");
                        node.put("expand", !expand);
                        shrinkSiblings(node);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            parent.addView(button);
        }
    }

    private boolean findPath(JSONObject root, JSONObject target, Collection<JSONObject> path) {

        // Base case if start is the end
        if (root == target) return true;

        JSONArray children = root.optJSONArray("nodes");
        if (children != null) {
            for (int i = 0; i < children.length(); i++) {
                JSONObject child = children.optJSONObject(i);
                if (child != null) {
                    if (findPath(child, target, path)) {
                        path.add(child);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void shrinkSiblings(final JSONObject node) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                HashSet<JSONObject> path = new HashSet<>();
                if (findPath(vocabJson, node, path)) {
                    path.add(vocabJson);
                    unmarkExcept(vocabJson, path);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                tableLayout.removeAllViews();
                addToView(vocabJson, 0);
            }
        }.execute();

    }

    private void unmarkExcept(JSONObject root, HashSet<JSONObject> path) {
        if (!path.contains(root)) {
            root.remove("expand");
        }
        JSONArray children = root.optJSONArray("nodes");
        if (children != null) {
            for (int i = 0; i < children.length(); i++) {
                JSONObject child = children.optJSONObject(i);
                if (child != null) {
                    unmarkExcept(child, path);
                }
            }
        }
    }

    private void initiate() {
        String vocabFilename = getString(R.string.vocab_file_name);
        vocabText = ExUtil.readFile(this, vocabFilename);
        if (vocabText.isEmpty()) {
            ExUtil.alert(this, "Vocabulary file is either absent or corrupted. Please load it first");
            finish();
        } else {
            parseVocabulary();
        }
    }
}
