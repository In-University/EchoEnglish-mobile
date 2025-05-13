package com.example.echoenglish_mobile.view.activity.webview; // Hoặc package của bạn

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.view.dialog.DictionaryBottomSheetDialog;

public class WebViewFragment extends Fragment {

    private static final String TAG = "WebViewFragmentWithPopup";
    private static final String JS_INTERFACE_TAG = "WebAppInterface";

    private static final String ARG_ANALYSIS_JSON = "analysis_json";

    private WebView webView;
    private String analysisJson;

    private boolean wordWrappingJsInjected = false;

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

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView START");
        View view = inflater.inflate(R.layout.activity_web_view, container, false);
        webView = view.findViewById(R.id.webView);

        wordWrappingJsInjected = false;

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);

        // Thêm JavaScript Interface
        webView.removeJavascriptInterface("AndroidInterface");
        webView.addJavascriptInterface(new WebAppInterface(requireContext()), "AndroidInterface");
        Log.d(TAG, "JavaScript Interface 'AndroidInterface' added.");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d(TAG, "Page started: " + url);
                wordWrappingJsInjected = false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, "Page finished: " + url);

                if (("file:///android_asset/index.html".equals(url) || url.startsWith("file:///android_asset/")) && analysisJson != null && !analysisJson.isEmpty()) {
                    Log.d(TAG, "index.html loaded. Injecting analysis data...");
                    injectDataAndRender(() -> {
                        Log.d(TAG, "Analysis data injection attempt finished. Now injecting word wrapping JS.");
                        if (!wordWrappingJsInjected) {
                            injectWordWrappingJs(view);
                            wordWrappingJsInjected = true;
                        }
                    });
                }
            }
        });

        if (analysisJson != null && !analysisJson.isEmpty()) {
            Log.d(TAG, "Loading initial asset for analysis: file:///android_asset/index.html");
            webView.loadUrl("file:///android_asset/index.html");
        }

        else {
            Log.w(TAG, "No specific content to load initially (e.g. analysisJson is null/empty). Loading blank or default index.html.");
            webView.loadUrl("file:///android_asset/index.html"); // Load index.html, onPageFinished sẽ xử lý
        }

        Log.d(TAG, "onCreateView END");
        return view;
    }

    private void injectWordWrappingJs(WebView targetWebView) {
        if (targetWebView == null) {
            Log.e(TAG, "Attempted to inject Word Wrapping JS but WebView was null!");
            return;
        }
        Log.d(TAG, "Preparing to inject Word Wrapping JS...");
        String js = "javascript:(function() { " +
                "   if (document.body.dataset.wordWrapped === 'true') { console.log('Word wrapping already applied.'); return; } "+ // Chống inject nhiều lần
                "   console.log('Starting word wrapping injection...'); " +
                "   function onWordClick(event) { " +
                "       event.stopPropagation(); " +
                "       var word = event.target.innerText.trim(); " +
                "       console.log('Word clicked: \"' + word + '\"'); " +
                "       if (word && word.length > 0 && word.length < 50) { " +
                "           try { AndroidInterface.processTextSelection(word); console.log('Called AndroidInterface for word: ' + word); } catch (err) { console.error('Error calling AndroidInterface: ' + err); } " +
                "       } else { console.log('Clicked word invalid or too long.'); } "+
                "   } " +
                "   function wrapWords(node, targetTags, ignoreTags) { " +
                "       if (!node) return; " +
                "       const nodeName = node.nodeName.toUpperCase(); "+
                "       if (ignoreTags.includes(nodeName) || node.classList && node.classList.contains('clickable-word')) { return; } "+ // Bỏ qua nếu đã là clickable-word
                "       let child = node.firstChild; "+
                "       while(child){ let nextChild = child.nextSibling; wrapWords(child, targetTags, ignoreTags); child = nextChild; } "+
                "       if (node.nodeType === 3 && node.parentNode && targetTags.includes(node.parentNode.nodeName.toUpperCase())) { " + // Node.TEXT_NODE là 3
                "           let textContent = node.nodeValue; " +
                "           let parts = textContent.split(/(\\s+)/).filter(part => part.length > 0); "+
                "           if (parts.length > 1 || (parts.length === 1 && !/^\\s+$/.test(parts[0]))) { "+
                "               let parent = node.parentNode; " +
                "               console.log('Wrapping text in:', parent.nodeName, ':', textContent.substring(0,30)+'...'); "+
                "               for (let i = 0; i < parts.length; i++) { " +
                "                   let part = parts[i]; " +
                "                   if (!/^\\s+$/.test(part)) { " +
                "                       let span = document.createElement('span'); span.textContent = part; span.classList.add('clickable-word'); span.addEventListener('click', onWordClick); parent.insertBefore(span, node); " +
                "                   } else { parent.insertBefore(document.createTextNode(part), node); } " +
                "               } " +
                "               parent.removeChild(node); " +
                "           } "+
                "       } " +
                "   } " +
                "   const TARGET_CONTAINER_TAGS = ['P', 'DIV', 'SPAN', 'LI', 'TD', 'H1', 'H2', 'H3', 'H4', 'H5', 'H6', 'ARTICLE', 'SECTION', 'BLOCKQUOTE', 'BODY']; "+ // Thêm BODY để chắc chắn
                "   const IGNORE_CHILDREN_TAGS = ['SCRIPT', 'STYLE', 'A', 'BUTTON', 'INPUT', 'TEXTAREA', 'SELECT', 'IMG', 'VIDEO', 'AUDIO', 'CANVAS', 'SVG', 'CODE', 'PRE', 'NOSCRIPT']; "+
                "   try { "+
                "       console.log('Calling wrapWords on document.body');"+
                "       wrapWords(document.body, TARGET_CONTAINER_TAGS, IGNORE_CHILDREN_TAGS); " +
                "       document.body.dataset.wordWrapped = 'true'; "+ // Đánh dấu đã wrap
                "       console.log('Word wrapping finished.'); " +
                "   } catch (err) { "+
                "        console.error('Error during word wrapping:', err); "+
                "   } "+
                "})();";

        targetWebView.evaluateJavascript(js, (String value) -> {
            Log.i(TAG, "Word Wrapping JS evaluated. Result: " + value);
        });
    }

    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void processTextSelection(String text) {
            if (getActivity() == null || getParentFragmentManager() == null) return;

            getActivity().runOnUiThread(() -> {
                try {
                    FragmentManager fm = getParentFragmentManager();
                    if (fm != null && !fm.isStateSaved() && !fm.isDestroyed()) {
                        DictionaryBottomSheetDialog.newInstance(text)
                                .show(fm, DictionaryBottomSheetDialog.TAG);
                    }
                } catch (Exception e) {
                    Log.e(JS_INTERFACE_TAG, "Failed to show DictionaryBottomSheetDialog", e);
                }
            });
        }


        @JavascriptInterface
        public void notifyContentRendered() {
            Log.d(JS_INTERFACE_TAG, "JS notified: content rendered.");

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "JS notified: content rendered.", Toast.LENGTH_SHORT).show();

                    if (webView != null && !wordWrappingJsInjected) {
                        injectWordWrappingJs(webView);
                        wordWrappingJsInjected = true;
                    }
                });
            }
        }
    }

    private void injectDataAndRender(Runnable onFinished) {
        if (analysisJson != null && !analysisJson.isEmpty()) {
            String jsCompatibleJson = analysisJson.replace("\\", "\\\\")
                    .replace("'", "\\'")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");

            final String jsCode = "javascript:analysisData = JSON.parse('" + jsCompatibleJson + "'); renderAnalysisContent();";


            if (webView != null) {
                Log.d(TAG, "Injecting analysis data via evaluateJavascript. JS (first 100 chars): " + jsCode.substring(0, Math.min(jsCode.length(),100)));
                webView.evaluateJavascript(jsCode, value -> {
                    Log.d(TAG, "JS for analysis data evaluated, result: " + value);
                    if (onFinished != null) {
                        onFinished.run();
                    }
                });
            }
        } else {
            showErrorInWebView("Lỗi: Dữ liệu phân tích rỗng khi cố gắng inject.");
            if (onFinished != null) {
                onFinished.run();
            }
        }
    }

    private void showErrorInWebView(String errorMessage) {
        if (webView == null) return;
        String escapedMessage = errorMessage.replace("'", "\\'");
        final String jsCode = "javascript:document.body.innerHTML = '<div style=\"color: red; padding: 20px;\">" + escapedMessage + "</div>';";
        webView.post(() -> webView.evaluateJavascript(jsCode, null));
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView called.");
        if (webView != null) {
            Log.d(TAG, "Cleaning up WebView.");
            if (webView.getParent() instanceof ViewGroup) {
                ((ViewGroup) webView.getParent()).removeView(webView);
            }
            webView.removeJavascriptInterface("AndroidInterface");
            webView.setWebViewClient(null);
            webView.setWebChromeClient(null);
            webView.stopLoading();
            webView.loadUrl("about:blank"); // Quan trọng để giải phóng tài nguyên
            webView.onPause(); // Nên gọi
            webView.removeAllViews();
            webView.destroyDrawingCache();
            webView.destroy();
            webView = null;
            Log.d(TAG, "WebView destroyed.");
        }
        super.onDestroyView();
    }
}