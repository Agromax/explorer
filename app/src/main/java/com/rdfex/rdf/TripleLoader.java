package com.rdfex.rdf;

import android.content.Context;

import com.rdfex.R;
import com.rdfex.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Dell on 10-07-2016.
 */
public class TripleLoader {


    private static final String[][] DUMMMY_TRIPLES = {
            {"co-1", "is", "semi_determinate_variety"},
            {"co-1_fruits", "are", "round_without_grooves"},
            {"co-1_yield", "is", "25_tonnes/ha"},
            {"co-2", "is", "indeterminate_variety"},
            {"co-2_fruits", "are", "flat_with_furrows"},
            {"co-2_yield", "is", "28-30_tonnes/ha"},
            {"co-3", "is", "suitable_for_close_planting"},
            {"co-3", "is", "determinate_variety"},
            {"co-3", "is", "induced_mutant"},
            {"co-3", "is", "high"},
            {"co-3", "is", "hybrid_between_hn-2_cln-2123a"},
            {"co-3_fruits", "are", "borne_in_clusters"},
            {"co-3_fruits", "are", "medium_sized"},
            {"co-3_fruits", "are", "smooth_round"},
            {"co-3_plants", "are", "suitable_for_high_density_planting"},
            {"co-3_plants", "are", "semi_determinate"},
            {"coth-1", "is", "hybrid_between_iihr-709_le-812"},
            {"coth-1_fruits", "are", "acidic"},
            {"coth-2", "recorded", "yield_of_t/ha"},
            {"coth-2", "recorded", "pesticide_spray"},
            {"coth-2", "recorded", "resistance_to_leaf_curl_virus_disease"},
            {"coth-2", "has", "pesticide_spray"},
            {"coth-2", "has", "resistance_to_leaf_curl_virus_disease"},
            {"coth-2", "is", "hybrid_between_lcr-2_cln-2123a"},
            {"coth-2_fruits", "are", "smooth_round"},
            {"paiyur-1", "is", "suitable_for_rainfed_cultivation"},
            {"paiyur-1", "is", "variety_evolved"},
            {"pkm-1", "is", "induced_mutant"},
            {"pkm-1_fruits", "are", "flat_round"},
            {"pkm-1_yield", "is", "30-35_tonnes/ha"}
    };

    /**
     * This method will load related triples
     *
     * @param query
     */
    public static ArrayList<Triple> loadTriple(Context context, String query) {
        ArrayList<Triple> triples = new ArrayList<>();
        /*
        for (String[] t : DUMMMY_TRIPLES) {
            if (t.length >= 3) {
                triples.add(new SPOTriple(t[0], t[1], t[2]));
            }
        }
        */

        String result = load(context, query);
        try {
            JSONObject res = new JSONObject(result);
            int code = res.getInt("code");
            if (code == 0) {
                JSONArray resArray = res.getJSONArray("msg");
                for (int i = 0; i < resArray.length(); i++) {
                    JSONObject o = resArray.getJSONObject(i);
                    String id = o.getString("_id");
                    String sub = o.getString("sub");
                    String pre = o.getString("pre");
                    String obj = o.getString("obj");
                    triples.add(new SPOTriple(id, sub, pre, obj));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return triples;
    }

    private static String load(Context context, CharSequence q) {
        Request request = new Request.Builder()
                .url(context.getString(R.string.query_url) + "?q=" + q)
                .build();

        try {
            Response response = Constants.HTTP_CLIENT.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
