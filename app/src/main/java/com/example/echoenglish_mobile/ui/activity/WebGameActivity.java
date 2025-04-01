package com.example.echoenglish_mobile.ui.activity;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.example.echoenglish_mobile.R;

public class WebGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_game);

        WebView webView = findViewById(R.id.webView);

        // Cấu hình WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);  // Bật JavaScript
        webSettings.setDomStorageEnabled(true);  // Hỗ trợ LocalStorage
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        // Load game
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl("https://wordwall.net/"); // Thay URL game tại đây

        /*
        - ENLISH
        https://wordwall.net/
        https://www.gamestolearnenglish.com/

        - GAME
        https://flappybird.io/
        https://www.google.com/logos/2010/pacman10-i.html
        https://learnenglish.britishcouncil.org/games/wordshake
        https://sudoku.com/
        https://tetris.com/play-tetris
        https://chromedino.com/

         */
    }
}
