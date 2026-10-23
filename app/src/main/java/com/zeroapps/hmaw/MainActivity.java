package com.zeroapps.hmaw;

import android.app.Activity;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    // Change these two to control the system bars
    private static final boolean SHOW_STATUS_BAR = false;
    private static final boolean SHOW_NAVIGATION_BAR = false;

    private static final String TARGET_URL = "https://www.google.com";

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupFullscreenLayout();

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        View container = findViewById(R.id.container);

        // Padding goes on the container, not on the WebView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            container.setOnApplyWindowInsetsListener((v, insets) -> {
                // Only bars that are visible give a non-zero value
                Insets bars = insets.getInsets(
                        WindowInsets.Type.statusBars()
                                | WindowInsets.Type.navigationBars());
                Insets cutout = insets.getInsets(WindowInsets.Type.displayCutout());

                int left = bars.left;
                int top = bars.top;
                int right = bars.right;
                int bottom = bars.bottom;

                if (SHOW_STATUS_BAR) {
                    top = Math.max(top, cutout.top);
                }
                if (SHOW_NAVIGATION_BAR) {
                    bottom = Math.max(bottom, cutout.bottom);
                }
                if (SHOW_STATUS_BAR || SHOW_NAVIGATION_BAR) {
                    left = Math.max(left, cutout.left);
                    right = Math.max(right, cutout.right);
                }

                v.setPadding(left, top, right, bottom);
                return WindowInsets.CONSUMED;
            });
        }

        // Hide the scrollbars
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog,
                                          boolean isUserGesture, Message resultMsg) {
                return false;
            }
        });

        webView.loadUrl(TARGET_URL);
    }

    private void setupFullscreenLayout() {
        boolean anyHidden = !SHOW_STATUS_BAR || !SHOW_NAVIGATION_BAR;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (anyHidden) {
                // Edge to edge; the inset listener adds padding for shown bars
                getWindow().setDecorFitsSystemWindows(false);

                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
                getWindow().setAttributes(params);
            } else {
                // Both bars shown: normal layout, the system handles everything
                getWindow().setDecorFitsSystemWindows(true);

                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
                getWindow().setAttributes(params);
            }
        } else if (!SHOW_STATUS_BAR && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(params);
        }
    }

    private void applySystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller == null) return;

            int toHide = 0;
            int toShow = 0;

            if (SHOW_STATUS_BAR) {
                toShow |= WindowInsets.Type.statusBars();
            } else {
                toHide |= WindowInsets.Type.statusBars();
            }

            if (SHOW_NAVIGATION_BAR) {
                toShow |= WindowInsets.Type.navigationBars();
            } else {
                toHide |= WindowInsets.Type.navigationBars();
            }

            if (toShow != 0) controller.show(toShow);
            if (toHide != 0) controller.hide(toHide);

            controller.setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        } else {
            int flags = View.SYSTEM_UI_FLAG_VISIBLE;

            if (!SHOW_STATUS_BAR) {
                flags |= View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            }
            if (!SHOW_NAVIGATION_BAR) {
                flags |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            }
            if (!SHOW_STATUS_BAR || !SHOW_NAVIGATION_BAR) {
                flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }

            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            applySystemBars();
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}