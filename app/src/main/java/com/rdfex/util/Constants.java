package com.rdfex.util;

import com.rdfex.ActiveUser;

import okhttp3.OkHttpClient;

/**
 * Created by Dell on 01-07-2016.
 */
public class Constants {
    public static final String VOCAB_BUNDLE_ARG_NAME = "vocab";
    public static final int GAP_WIDTH_QUANTA = 100;
    public static final String TERM_NAME = "term";
    public static final String RDF_ID = "rdf-id";


    public static final String LOGIN_RESULT = "login-result";

    public static ActiveUser ACTIVE_USER = null;

    public static final OkHttpClient HTTP_CLIENT = new OkHttpClient();
}
