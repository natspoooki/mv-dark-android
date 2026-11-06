package com.example.mvdark;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private View loadingLayout;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mediación Virtual");
        }

        prefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);

        webView = findViewById(R.id.webview);
        loadingLayout = findViewById(R.id.loading_layout);

        applyBaseBackgrounds();

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
            mlp.topMargin = insets.top;
            toolbar.setLayoutParams(mlp);
            return WindowInsetsCompat.CONSUMED;
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                loadingLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // Only apply custom dark mode scripts when outside the login flow
                if (url != null && !url.contains("/login/")) {
                    webView.loadUrl(getCustomCssScript());
                }

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    loadingLayout.setVisibility(View.GONE);
                }, 150);
            }
        });

        webView.loadUrl("https://mv.mediacionvirtual.ucr.ac.cr/my/");

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    setEnabled(false);
                    onBackPressed();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyBaseBackgrounds();
        if (webView != null) {
            webView.reload();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void applyBaseBackgrounds() {
        int bg = prefs.getInt("col_bg", Color.parseColor("#121212"));
        webView.setBackgroundColor(bg);
        loadingLayout.setBackgroundColor(bg);
    }

    private String getCustomCssScript() {
        int bg = prefs.getInt("col_bg", Color.parseColor("#121212"));
        int navbar = prefs.getInt("col_navbar", Color.parseColor("#181818"));
        int textboxBg = prefs.getInt("col_textbox", Color.parseColor("#1e1e1e"));
        int card = prefs.getInt("col_card", Color.parseColor("#1e1e1e"));
        int button = prefs.getInt("col_button", Color.parseColor("#0055a5"));
        int accent = prefs.getInt("col_accent", Color.parseColor("#8ab4f8"));
        int subtitle = prefs.getInt("col_subtitle", Color.parseColor("#aaaaaa"));

        String bgHex = String.format("#%06X", (0xFFFFFF & bg));
        String navbarHex = String.format("#%06X", (0xFFFFFF & navbar));
        String textboxHex = String.format("#%06X", (0xFFFFFF & textboxBg));
        String cardHex = String.format("#%06X", (0xFFFFFF & card));
        String buttonHex = String.format("#%06X", (0xFFFFFF & button));
        String accentHex = String.format("#%06X", (0xFFFFFF & accent));
        String subtitleHex = String.format("#%06X", (0xFFFFFF & subtitle));
        String textHex = "#e0e0e0";

        String script = "javascript:(function() {" +
                "var styleId = 'granular-studio-style';" +
                "var style = document.getElementById(styleId);" +
                "if (!style) {" +
                "style = document.createElement('style');" +
                "style.id = styleId;" +
                "document.head.appendChild(style);" +
                "}" +
                "style.innerHTML = `" +
                "html, body, #page-wrapper, .wrapper, #page { background-color: " + bgHex + " !important; color: " + textHex + " !important; }" +
                ".navbar, header.navbar, .custom-navbar, .login-navbar, nav.navbar, .navbar.bg-white, " +
                ".navbar-toggler, .popover-region-toggle, .usermenu-container, .usermenu, .usermenu .dropdown-toggle, #user-menu-toggle { background-color: " + navbarHex + " !important; color: " + textHex + " !important; border-color: transparent !important; box-shadow: none !important; }" +
                "input, select, textarea, .form-control { background-color: " + textboxHex + " !important; color: " + textHex + " !important; border-color: #555555 !important; }" +
                ".card, .dashboard-card, .main-inner, #region-main, .box, .activity-item, .card-body, .course-content, .list-group, .list-group-item, .card-footer, .card-footer.menu, .secondary-navigation, .availabilityinfo, .isrestricted { background-color: " + cardHex + " !important; color: " + textHex + " !important; border-color: #333333 !important; }" +
                ".drawer, .drawerheader, .drawercontent, .drag-container { background-color: " + cardHex + " !important; color: " + textHex + " !important; border-color: #333333 !important; }" +
                ".drawer .list-group-item { background-color: " + cardHex + " !important; color: " + textHex + " !important; border-color: #333333 !important; }" +
                ".drawer .list-group-item.active { background-color: " + buttonHex + " !important; color: #ffffff !important; }" +
                ".popover-region-container, .popover-region-header-container, .popover-region-content-container, .popover-region-content, .all-notifications, .popover-region-footer-container { background-color: " + cardHex + " !important; color: " + textHex + " !important; border-color: #333333 !important; }" +
                ".popover, .popover-body, .popover-header { background-color: " + cardHex + " !important; color: " + textHex + " !important; border-color: #333333 !important; }" +
                ".message-app, [data-region='message-drawer'], .header-container, .body-container, .view-conversation { background-color: " + cardHex + " !important; color: " + textHex + " !important; }" +
                ".message-app .bg-white, .message-app .bg-light, [data-region='message-drawer'] .bg-white, [data-region='message-drawer'] .bg-light { background-color: " + cardHex + " !important; color: " + textHex + " !important; }" +
                ".popover-region-footer-container, .see-all-link { background-color: " + cardHex + " !important; color: " + accentHex + " !important; }" +
                ".secondary-navigation, .secondary-navigation .moremenu, .secondary-navigation .navigation { background-color: " + cardHex + " !important; border: none !important; box-shadow: none !important; margin: 0 !important; padding: 4px !important; }" +
                ".nav-tabs { background-color: " + cardHex + " !important; border-bottom: 1px solid #333333 !important; }" +
                ".nav-tabs .nav-item { background-color: " + cardHex + " !important; }" +
                ".nav-tabs .nav-link { background-color: transparent !important; color: " + textHex + " !important; border: 1px solid transparent !important; }" +
                ".nav-tabs .nav-link:hover { border-color: #444444 !important; }" +
                ".nav-tabs .nav-link.active { background-color: " + cardHex + " !important; color: " + accentHex + " !important; border-color: #333333 #333333 " + cardHex + " !important; }" +
                ".dropdown-menu, .dropdown-item, .navbar .dropdown-menu, [data-region='action-menu'] { background-color: " + cardHex + " !important; color: " + textHex + " !important; border-color: #333333 !important; }" +
                ".dropdown-item:hover, .dropdown-item:focus { background-color: " + buttonHex + " !important; color: #ffffff !important; }" +
                ".dropdown-divider, .popover-header { border-color: #333333 !important; }" +
                ".text-muted, .dimmed_text, small, .card-text, .course-description, .categoryname { color: " + subtitleHex + " !important; }" +
                "button:not(.navbar-toggler):not(.popover-region-toggle), .btn:not(.navbar-toggler), .btn-primary, .singlebutton button, .coursemenubtn { background-color: " + buttonHex + " !important; border-color: " + buttonHex + " !important; color: #ffffff !important; box-shadow: none !important; outline: none !important; }" +
                "a:not(.navbar-brand):not(.nav-link) { color: " + accentHex + " !important; }" +
                ".message-app .message, [data-region='message-drawer'] .message, .message.received, .message.send, .message-app .bg-light, .message-app .text-bg-light { background-color: " + textboxHex + " !important; color: " + textHex + " !important; border-color: #444444 !important; }" +
                ".message-app .message.send { background-color: " + buttonHex + " !important; color: #ffffff !important; }" +
                ".message-app p, .message-app span, .message-app div { color: inherit; }" +
                ".filter-group, [data-table-region*='participants'], .table-dynamic, .generaltable { background-color: " + cardHex + " !important; color: " + textHex + " !important; border-color: #333333 !important; }" +
                ".filter-group .bg-white, .filter-group .bg-light { background-color: " + cardHex + " !important; color: " + textHex + " !important; border-color: #333333 !important; }" +
                ".table-striped > tbody > tr:nth-of-type(odd) > * { background-color: " + cardHex + " !important; color: " + textHex + " !important; }" +
                ".table-striped > tbody > tr:nth-of-type(even) > * { background-color: " + textboxHex + " !important; color: " + textHex + " !important; }" +
                ".table-hover > tbody > tr:hover > * { background-color: #2a2a2a !important; color: " + textHex + " !important; }" +
                ".initialbar .page-item .page-link, .pagination .page-item .page-link { background-color: " + textboxHex + " !important; color: " + accentHex + " !important; border-color: #333333 !important; }" +
                ".initialbar .page-item.active .page-link, .pagination .page-item.active .page-link { background-color: " + buttonHex + " !important; color: #ffffff !important; border-color: " + buttonHex + " !important; }" +
                ".initialbarlabel, .initialbar .page-item { color: " + subtitleHex + " !important; }" +
                "`;" +
                "})();";

        return script;
    }
}