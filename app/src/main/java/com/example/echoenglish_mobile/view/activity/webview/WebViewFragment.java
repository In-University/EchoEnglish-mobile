package com.example.echoenglish_mobile.view.activity.webview;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.echoenglish_mobile.R;

public class WebViewFragment extends Fragment {

    private static final String ARG_ANALYSIS_JSON = "analysis_json";

    private WebView webView;
    private String analysisJson;

    public static WebViewFragment newInstance(String analysisJson) {
        WebViewFragment fragment = new WebViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ANALYSIS_JSON, analysisJson);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            analysisJson = getArguments().getString(ARG_ANALYSIS_JSON);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_web_view, container, false);
        webView = view.findViewById(R.id.webView);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if ("file:///android_asset/index.html".equals(url)) {
                    if (analysisJson != null && !analysisJson.isEmpty()) {
                        injectDataAndRender();
                    } else {
                        showErrorInWebView("Error: Analysis data is missing.");
                    }
                }
            }
        });

        webView.loadUrl("file:///android_asset/index.html");

        return view;
    }

    private void injectDataAndRender() {
        if (analysisJson != null && !analysisJson.isEmpty()) {
            final String jsCode = "javascript:analysisData = " + analysisJson + "; renderAnalysisContent();";

            if (webView != null) {
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        webView.evaluateJavascript(jsCode, null);
                    }
                });
            }
//            System.out.println("Executed JS: " + jsCode);
        } else {
            showErrorInWebView("Error: Analysis data is null or empty when trying to inject.");
        }
    }

    private void showErrorInWebView(String errorMessage) {
        if (webView == null) return;

        String escapedMessage = errorMessage.replace("'", "\\'");
        final String jsCode = "javascript:document.body.innerHTML = '<div style=\"color: red; padding: 20px;\">" + escapedMessage + "</div>';";

        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.evaluateJavascript(jsCode, null);
            }
        });
    }
}