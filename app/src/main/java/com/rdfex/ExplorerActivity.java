package com.rdfex;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ExplorerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explorer);

        init();
    }

    private void init() {
        final WebView webView = (WebView) findViewById(R.id.web_view);
        if (webView != null) {
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.endsWith("exit")) {
                        finish();
                    } else {
                        webView.loadUrl(url);
                    }
                    return true;
                }
            });
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);

            webView.loadUrl(getString(R.string.explorer_url));
        }
    }

}
