package com.niftyalgo.app;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/** Thin shell: shows the Nifty Algo portal (appUrl) full screen, keeping cookies/login. */
public class MainActivity extends AppCompatActivity {

    private WebView web;
    private SwipeRefreshLayout refresh;
    private boolean failed = false;
    private String appUrl;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appUrl = getString(R.string.app_url);

        web = new WebView(this);
        refresh = new SwipeRefreshLayout(this);
        refresh.addView(web, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(refresh);
        refresh.setOnRefreshListener(() -> web.reload());
        // only allow pull-to-refresh when the page is scrolled to the very top
        refresh.setOnChildScrollUpCallback((parent, child) -> web.getScrollY() > 0);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);

        // keep the portal sign-in cookie (and the Shoonya redirect cookie) in this WebView
        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(web, true);

        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                // Stay inside the app for every http(s) page, so the Shoonya login and
                // its redirect back to /shoonya/callback share the same cookies.
                String scheme = r.getUrl().getScheme();
                return !("http".equals(scheme) || "https".equals(scheme));
            }

            @Override
            public void onPageStarted(WebView v, String url, Bitmap icon) {
                failed = false;
            }

            @Override
            public void onPageFinished(WebView v, String url) {
                refresh.setRefreshing(false);
                CookieManager.getInstance().flush();
            }

            @Override
            public void onReceivedError(WebView v, WebResourceRequest r, WebResourceError e) {
                if (r.isForMainFrame()) {
                    failed = true;
                    refresh.setRefreshing(false);
                    v.loadDataWithBaseURL(null, errorHtml(e.getDescription().toString()),
                            "text/html", "utf-8", null);
                }
            }

            @Override
            public void onReceivedSslError(WebView v, SslErrorHandler h, SslError e) {
                h.cancel(); // never bypass certificate problems
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (web.canGoBack() && !failed) web.goBack();
                else finish();
            }
        });

        if (savedInstanceState != null) web.restoreState(savedInstanceState);
        else web.loadUrl(appUrl);
    }

    private String errorHtml(String why) {
        return "<html><head><meta name='viewport' content='width=device-width,initial-scale=1'></head>"
                + "<body style='background:#0b1220;color:#e6edf7;font-family:sans-serif;text-align:center;padding:40px 20px'>"
                + "<h2>Can't reach Nifty Algo</h2><p style='color:#8fa0bf'>" + why + "</p>"
                + "<p style='color:#8fa0bf;font-size:13px'>" + appUrl + "</p>"
                + "<button onclick=\"location.href='" + appUrl + "'\" "
                + "style='background:#2de6a8;border:0;border-radius:10px;padding:12px 28px;font-size:16px;font-weight:700'>Retry</button>"
                + "</body></html>";
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override protected void onPause()  { super.onPause();  CookieManager.getInstance().flush(); web.onPause(); }
    @Override protected void onResume() { super.onResume(); web.onResume(); }
}
