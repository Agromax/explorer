package com.rdfex.util;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Dell on 22-07-2016.
 */
public class TermSuggestionProvider extends ContentProvider {

    private final Context context = getContext();
    private List<String> terms;

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (terms == null || terms.isEmpty()) {
            Request req = new Request.Builder()
                    .url("http://192.168.1.3:3000/terms")
                    .build();
            try {
                Response response = Constants.HTTP_CLIENT.newCall(req).execute();
                String result = response.body().string();
                terms = new ArrayList<>();
                String[] tokens = result.split("[\n\r]+");
                terms.addAll(Arrays.asList(tokens));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        MatrixCursor cursor = new MatrixCursor(
                new String[]{
                        BaseColumns._ID,
                        SearchManager.SUGGEST_COLUMN_TEXT_1,
                        SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
                }
        );

        if (terms != null) {
            String query = uri.getLastPathSegment().toUpperCase();
            int limit = Integer.parseInt(uri.getQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT));
            int length = terms.size();
            for (int i = 0; i < length && cursor.getCount() < limit; i++) {
                String term = terms.get(i);
                if (term.toUpperCase().contains(query)) {
                    cursor.addRow(new Object[]{i, term, i});
                }
            }
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
