package com.rittz.hackathon;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity {

    private WebView webview;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webview = (WebView) findViewById(R.id.webview);

        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setGeolocationEnabled(true);
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
               callback.invoke(origin, true, false);
            }
           });
        webview.setWebViewClient(webClient);

        webview.loadUrl(getString(R.string.base_url));
    }

    private final WebViewClient webClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
            view.loadUrl(url);
            return true;
        }
    };

    @Override
    public void onBackPressed() {
        if(webview.canGoBack() == true){
            webview.goBack();
        }
        else {
            super.onBackPressed();
        }
    }
}
