package com.example.mvdark;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private WebView previewWebView;
    private int colBg, colNavbar, colTextboxBg, colCard, colButton, colAccent, colSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Color Studio");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
            mlp.topMargin = insets.top;
            toolbar.setLayoutParams(mlp);
            return WindowInsetsCompat.CONSUMED;
        });

        prefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        loadSavedColors();

        previewWebView = findViewById(R.id.preview_webview);
        WebSettings settings = previewWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        previewWebView.addJavascriptInterface(new WebAppInterface(), "AndroidBridge");

        previewWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                applyColorsAndIsolateElements();
            }
        });

        previewWebView.loadUrl("https://mv.mediacionvirtual.ucr.ac.cr/my/");

        findViewById(R.id.btn_save).setOnClickListener(v -> {
            saveColors();
            finish();
        });

        findViewById(R.id.btn_reset).setOnClickListener(v -> {
            resetToDefaults();
            applyColorsAndIsolateElements();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void onTargetClicked(String targetType) {
            runOnUiThread(() -> {
                if (targetType == null) return;
                switch (targetType) {
                    case "navbar":
                        showVisualColorPicker("Navbar Color", colNavbar, c -> { colNavbar = c; applyColorsAndIsolateElements(); });
                        break;
                    case "card":
                        showVisualColorPicker("Course Cards Color", colCard, c -> { colCard = c; applyColorsAndIsolateElements(); });
                        break;
                    case "textbox":
                        showVisualColorPicker("Textbox Backgrounds Color", colTextboxBg, c -> { colTextboxBg = c; applyColorsAndIsolateElements(); });
                        break;
                    case "button":
                        showVisualColorPicker("Detail / Action Button Color", colButton, c -> { colButton = c; applyColorsAndIsolateElements(); });
                        break;
                    case "accent":
                        showVisualColorPicker("Accent Links Color", colAccent, c -> { colAccent = c; applyColorsAndIsolateElements(); });
                        break;
                    case "subtitle":
                        showVisualColorPicker("Course Subtitle / Muted Text Color", colSubtitle, c -> { colSubtitle = c; applyColorsAndIsolateElements(); });
                        break;
                    default:
                        showVisualColorPicker("Page Background Color", colBg, c -> { colBg = c; applyColorsAndIsolateElements(); });
                        break;
                }
            });
        }
    }

    private void applyColorsAndIsolateElements() {
        String bgHex = String.format("#%06X", (0xFFFFFF & colBg));
        String navbarHex = String.format("#%06X", (0xFFFFFF & colNavbar));
        String textboxHex = String.format("#%06X", (0xFFFFFF & colTextboxBg));
        String cardHex = String.format("#%06X", (0xFFFFFF & colCard));
        String buttonHex = String.format("#%06X", (0xFFFFFF & colButton));
        String accentHex = String.format("#%06X", (0xFFFFFF & colAccent));
        String subtitleHex = String.format("#%06X", (0xFFFFFF & colSubtitle));
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

                "function bindClick(selector, type) {" +
                "document.querySelectorAll(selector).forEach(el => {" +
                "el.onclick = function(e) {" +
                "e.preventDefault();" +
                "e.stopPropagation();" +
                "AndroidBridge.onTargetClicked(type);" +
                "};" +
                "});" +
                "}" +

                "bindClick('.navbar, header.navbar, .custom-navbar, .usermenu-container', 'navbar');" +
                "bindClick('.card, .dashboard-card, .main-inner, .activity-item, .list-group-item', 'card');" +
                "bindClick('input, select, textarea, .form-control', 'textbox');" +
                "bindClick('button:not(.navbar-toggler), .btn:not(.navbar-toggler), .coursemenubtn', 'button');" +
                "bindClick('a', 'accent');" +
                "bindClick('.text-muted, .dimmed_text, .categoryname, .card-text', 'subtitle');" +

                "document.body.onclick = function(e) {" +
                "if(e.target === document.body || e.target === document.documentElement || e.target.id === 'page-wrapper') {" +
                "AndroidBridge.onTargetClicked('bg');" +
                "}" +
                "};" +
                "})();";

        previewWebView.loadUrl(script);
    }

    private void showVisualColorPicker(String title, int currentColor, OnColorSelectedListener listener) {
        String[] swatchNames = {
                "Classic Charcoal (#121212)", "AMOLED Black (#000000)", "Midnight Blue (#0f172a)",
                "Dark Slate (#1e293b)", "Pure White (#ffffff)", "UCR Blue (#0055a5)",
                "Electric Blue (#38bdf8)", "Mint Green (#34d399)", "Warm Amber (#ffb86c)", "Soft Red (#f87171)"
        };
        int[] swatchValues = {
                Color.parseColor("#121212"), Color.parseColor("#000000"), Color.parseColor("#0f172a"),
                Color.parseColor("#1e293b"), Color.parseColor("#ffffff"), Color.parseColor("#0055a5"),
                Color.parseColor("#38bdf8"), Color.parseColor("#34d399"), Color.parseColor("#ffb86c"), Color.parseColor("#f87171")
        };

        new AlertDialog.Builder(this)
                .setTitle("Pick " + title)
                .setItems(swatchNames, (dialog, which) -> listener.onColorSelected(swatchValues[which]))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadSavedColors() {
        colBg = prefs.getInt("col_bg", Color.parseColor("#121212"));
        colNavbar = prefs.getInt("col_navbar", Color.parseColor("#181818"));
        colTextboxBg = prefs.getInt("col_textbox", Color.parseColor("#1e1e1e"));
        colCard = prefs.getInt("col_card", Color.parseColor("#1e1e1e"));
        colButton = prefs.getInt("col_button", Color.parseColor("#0055a5"));
        colAccent = prefs.getInt("col_accent", Color.parseColor("#8ab4f8"));
        colSubtitle = prefs.getInt("col_subtitle", Color.parseColor("#aaaaaa"));
    }

    private void saveColors() {
        prefs.edit()
                .putInt("col_bg", colBg)
                .putInt("col_navbar", colNavbar)
                .putInt("col_textbox", colTextboxBg)
                .putInt("col_card", colCard)
                .putInt("col_button", colButton)
                .putInt("col_accent", colAccent)
                .putInt("col_subtitle", colSubtitle)
                .apply();
    }

    private void resetToDefaults() {
        colBg = Color.parseColor("#121212");
        colNavbar = Color.parseColor("#181818");
        colTextboxBg = Color.parseColor("#1e1e1e");
        colCard = Color.parseColor("#1e1e1e");
        colButton = Color.parseColor("#0055a5");
        colAccent = Color.parseColor("#8ab4f8");
        colSubtitle = Color.parseColor("#aaaaaa");
    }

    interface OnColorSelectedListener {
        void onColorSelected(int color);
    }
}