package com.padimas.midtransflutter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.RequiresApi;

import com.midtrans.sdk.uikit.activities.BaseActivity;

abstract class WebviewBaseActivity extends BaseActivity {

    public static final String EXTRA_URL = "url";

    protected WebView webView;
    protected String url;


    @SuppressLint("SetJavaScriptEnabled")
    protected void populateWebview(FrameLayout webViewContainer) {

        url = getIntent().getStringExtra(EXTRA_URL);
        if (webView == null) {
            webView = new WebView(this);
            webView.clearCache(true);
            webView.getSettings().setJavaScriptEnabled(true);
            setupWebViewClient();
            webView.loadUrl(url);
        }

        webViewContainer.addView(webView);
    }

    @Override
    public void onBackPressed() {
        if (webView != null) {
            webView.destroy();
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }

    protected abstract void setupWebViewClient();

}

class WebviewVerifyActivity extends WebviewBaseActivity {

    public static final String SUCCESS_URL = "/token/callback/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        FrameLayout webViewContainer = (FrameLayout) findViewById(R.id.webview_container);

        populateWebview(webViewContainer);
    }


    @Override
    protected void setupWebViewClient() {
        webView.setWebViewClient(new MyWebClient());
        webView.setWebChromeClient(new WebChromeClient());
    }

    private void completePayment() {
        setResult(RESULT_OK);
        finish();
    }

    private void paymentFailed(String message) {
        Intent resultIntent = new Intent();
        // TODO Add extras or a data URI to this intent as appropriate.
        resultIntent.putExtra("message", message);
        setResult(Activity.RESULT_CANCELED, resultIntent);
        finish();
    }

    public class MyWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            //Timber.i("Load url : %s", url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //Timber.i("Overloading url : %s", url);
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (url.contains(SUCCESS_URL)) {
                if (url.contains(SUCCESS_URL)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        checkingToken(view);
                    } else {
                        completePayment();
                    }

                }
            }
            super.onPageFinished(view, url);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        private void checkingToken(WebView view) {
            view.evaluateJavascript(
                    "(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();",
                    new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String html) {
                            Log.d("HTML", html);
                            if (html.contains("Success")) {
                                completePayment();
                            } else if (html.contains("Card is not authenticated")) {
                                paymentFailed("Card is not authenticated.");
                            } else if (html.contains("Failed to generate 3D Secure token")) {
                                paymentFailed("Failed to generate 3D Secure token.");
                            } else {
                                paymentFailed("Unknown error");
                            }
                        }
                    });
        }
    }

}
