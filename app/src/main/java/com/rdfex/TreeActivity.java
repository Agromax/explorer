package com.rdfex;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

        setTitle("Vocabulary Explorer");

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
            final Button button = new Button(this);

            Drawable sign = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_add_black_24dp, null);
            sign.setBounds(0, 0, sign.getMinimumWidth(), sign.getMinimumHeight());
            boolean isExpanded = node.optBoolean("expand", false);
            if (isExpanded) {
                sign = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_remove_black_24dp, null);
                sign.setBounds(0, 0, sign.getMinimumWidth(), sign.getMinimumHeight());
            }
            button.setCompoundDrawables(sign, null, null, null);
//            button.setBackground(ContextCompat.getDrawable(this, R.drawable.tree_node_btn));
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

    private int computeNodalPath(JSONObject node, JSONObject target, HashSet<JSONObject> path) {
        int len = 0;
        JSONObject p = node;

        while (p != target) {
            JSONArray children = p.optJSONArray("nodes");
            for (int i = 0; i < children.length(); i++) {
                len++;
                JSONObject child = children.optJSONObject(i);
                if (path.contains(child)) {
                    p = child;
                    break;
                }
            }
        }

        return len;
    }

    private void shrinkSiblings(final JSONObject node) {
        new AsyncTask<Void, Void, Void>() {
            private HashSet<JSONObject> path = null;
            private int nodalLength = 0;

            @Override
            protected Void doInBackground(Void... params) {
                path = new HashSet<>();
                if (findPath(vocabJson, node, path)) {
                    nodalLength = computeNodalPath(vocabJson, node, path);
//                    System.out.println("Path length: " + path.size());
//                    for (JSONObject pp : path) {
//                        System.out.print(pp.optString("text") + " ");
//                    }
//                    System.out.println();
//                    System.out.println("Nodal Length = " + nodalLength + " yahan per gadbad hai!");
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
/*
                int cnt = 0;
                for (JSONObject o : currentVisible) {
                    if (o == node) {
                        break;
                    } else {
                        cnt++;
                    }
                }*/
                System.out.println("nl: " + nodalLength * 80);
                final ScrollView scrollView = (ScrollView) findViewById(R.id.scroll);
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.smoothScrollTo(0, nodalLength * 90);
                    }
                });


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
        search.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                handleSearch();
                return false;
            }
        });
        return true;
    }

    private void handleSearch() {
        Intent intent = new Intent(this, SearchTermActivity.class);
        startActivityForResult(intent, Constants.SEARCH_TERM_REQUEST_CODE);
    }


    private JSONObject findNodeFromText(JSONObject u, String name) {
        String text = u.optString("text");
        if (text != null && text.equals(name)) return u;
        JSONArray children = u.optJSONArray("nodes");
        if (children != null) {
            for (int i = 0; i < children.length(); i++) {
                JSONObject v = children.optJSONObject(i);
                JSONObject w = findNodeFromText(v, name);
                if (w != null) return w;
            }
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.SEARCH_TERM_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                final String queryTerm = data.getStringExtra("queryTerm");
                System.out.println("Query Term: " + queryTerm);
                new AsyncTask<Void, Void, JSONObject>() {
                    HashSet<JSONObject> path = new HashSet<>();

                    @Override
                    protected JSONObject doInBackground(Void... params) {
                        JSONObject node = findNodeFromText(vocabJson, queryTerm);
                        findPath(vocabJson, node, path);
                        for (JSONObject n : path) {
                            try {
                                n.put("expand", true);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        return node;
                    }

                    @Override
                    protected void onPostExecute(JSONObject node) {
                        if (node != null) {
                            System.out.println("Node is not null  " + node.optString("text"));
                            shrinkSiblings(node);
                        }
                    }
                }.execute();
            }
        }
    }
}
