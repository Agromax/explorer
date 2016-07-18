package com.rdfex;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TableLayout;

import com.rdfex.util.Constants;
import com.rdfex.util.ExUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author Anurag Gautam
 * @version $revision-history:$ 010716, 020716
 */
public class TreeActivity extends AppCompatActivity {

    private String vocabText = null;
    private JSONObject vocabJson = null;
    private TableLayout tableLayout = null;
    private ArrayList<JSONObject> currentVisible = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree);

        // The main layout that displays the data graphically
        tableLayout = (TableLayout) findViewById(R.id.table);
        /*if (tableLayout != null) {
            tableLayout.removeAllViews();
        }*/

        setTitle("Vocabulary Browser");

        // Start the process
        initiate();
    }


    /**
     * The text in the vocab file is a string dump of a JSON object. So this method parses that
     * string and converts it to an actual JSON object.
     */
    private void parseVocabulary() {
        if (vocabText != null) {
            try {
                vocabJson = new JSONObject(vocabText);
                if (tableLayout != null) {
                    // Clear the table
                    tableLayout.removeAllViews();
                }
                addToView(vocabJson, 0);
            } catch (JSONException e) {
                e.printStackTrace();
                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage(e.getMessage())
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).create()
                        .show();
            }
        }
    }

    /**
     * Visualizes the JSON vocabulary as a tree structure
     *
     * @param root
     * @param tabIndex
     */
    private void addToView(JSONObject root, int tabIndex) {
        int width = computeWidth(tabIndex);

        LinearLayout ll = new LinearLayout(this);
        addSpace(ll, width);
        addContent(ll, root);
        tableLayout.addView(ll);
        currentVisible.add(root);

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
        final String term = node.optString("text", null);
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

            ImageButton viewRDF = new ImageButton(this);
            viewRDF.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_launch_black_24dp));
            viewRDF.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleViewRDF(term);
                }
            });

            parent.addView(button);
            parent.addView(viewRDF);
        }
    }

    private void handleViewRDF(final String term) {
        System.out.println("Browsing term: " + term);
        Intent intent = new Intent(this, ViewerActivity.class);
        intent.putExtra(Constants.TERM_NAME, term);
        startActivity(intent);
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
            private HashSet<JSONObject> path = null;

            @Override
            protected Void doInBackground(Void... params) {
                path = new HashSet<>();
                if (findPath(vocabJson, node, path)) {
                    path.add(vocabJson);
                    unmarkExcept(vocabJson, path);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                tableLayout.removeAllViews();
                currentVisible.clear();
                addToView(vocabJson, 0);

                int cnt = 0;
                for (JSONObject o : currentVisible) {
                    if (o == node) {
                        break;
                    } else {
                        cnt++;
                    }
                }
                ScrollView scrollView = (ScrollView) findViewById(R.id.scroll);
                scrollView.scrollTo(0, cnt * 60);
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

    /**
     * Loads vocabulary from the vocab file and renders it as a tree. If the
     * vocabulary is not found it displays an error message and finishes the activity
     */
    private void initiate() {
        String vocabFilename = getString(R.string.vocab_file_name);
        vocabText = ExUtil.readFile(this, vocabFilename);
        if (vocabText.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage(getString(R.string.vocab_not_found))
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .create()
                    .show();
        } else {
            parseVocabulary();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.treeview_menu, menu);

        MenuItem search = menu.findItem(R.id.search_btn);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);

        // TODO View https://androidhub.intel.com/en/posts/nglauber/Android_Search.html
        //
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }
}
