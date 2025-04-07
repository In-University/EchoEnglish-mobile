package com.example.echoenglish_mobile.ui.activity;

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
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.echoenglish_mobile.R;

public class BrowserFragment extends Fragment {

    // Consistent Tag for Logging
    private static final String BROWSER_TAG = "BrowserFragment";
    private static final String JS_INTERFACE_TAG = "WebAppInterface";

    private static final String ARG_URL = "url";
    private WebView webView;
    private ProgressBar progressBar;
    private String currentUrl;

    // Flag to prevent injecting JS multiple times if onPageFinished is called again
    private boolean wordWrappingJsInjected = false;

    public static BrowserFragment newInstance(String url) {
        BrowserFragment fragment = new BrowserFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(BROWSER_TAG, "onCreate");
        if (getArguments() != null) {
            currentUrl = getArguments().getString(ARG_URL);
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(BROWSER_TAG, "onCreateView START");
        View view = inflater.inflate(R.layout.fragment_browser, container, false);
        webView = view.findViewById(R.id.webView);
        progressBar = view.findViewById(R.id.progressBar);
        wordWrappingJsInjected = false; // Reset flag when view is created

        // --- WebView Settings ---
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true); // May be needed by some JS

        // --- Add JavaScript Interface ---
        // Ensure the interface isn't added multiple times if view is recreated
        webView.removeJavascriptInterface("AndroidInterface"); // Remove any old instance first
        webView.addJavascriptInterface(new WebAppInterface(requireContext()), "AndroidInterface");
        Log.d(BROWSER_TAG, "JavaScript Interface 'AndroidInterface' added.");

        // --- WebViewClient ---
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(BROWSER_TAG, "WebViewClient: Page finished loading: " + url);
                if(progressBar != null) progressBar.setVisibility(View.GONE);

                // Inject word wrapping JS *only once* per page load sequence
                if (!wordWrappingJsInjected) {
                    Log.d(BROWSER_TAG, "Injecting word wrapping JS...");
                    injectWordWrappingJs(view);
                    wordWrappingJsInjected = true;
                } else {
                    Log.d(BROWSER_TAG, "Word wrapping JS already injected for this load.");
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d(BROWSER_TAG, "WebViewClient: Page started loading: " + url);
                wordWrappingJsInjected = false; // Reset flag on new page start
                if(progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(0);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(BROWSER_TAG, "WebViewClient: Loading new URL: " + url);
                wordWrappingJsInjected = false; // Reset flag when navigating away
                view.loadUrl(url);
                return true;
            }
        });

        // --- WebChromeClient ---
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if(progressBar != null) {
                    progressBar.setProgress(newProgress);
                    if (newProgress == 100) {
                        progressBar.setVisibility(View.GONE);
                    } else {
                        if (progressBar.getVisibility() == View.GONE && newProgress < 100) {
                            progressBar.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("WebViewConsole", consoleMessage.message() + " -- From line "
                        + consoleMessage.lineNumber() + " of "
                        + consoleMessage.sourceId());
                return true;
            }
        });


        if (currentUrl != null) {
            Log.d(BROWSER_TAG, "Loading initial URL: " + currentUrl);
            webView.loadUrl(currentUrl);
        } else {
            Log.w(BROWSER_TAG, "Initial URL is null!");
        }
        Log.d(BROWSER_TAG, "onCreateView END");
        return view;
    }

    // --- NEW: Inject JavaScript to wrap words in spans and add click listeners ---
    private void injectWordWrappingJs(WebView targetWebView) {
        // This JS is complex and might need adjustments depending on target websites
        String js = "javascript:(function() { " +
                "   console.log('Starting word wrapping...'); " +
                // --- Function to handle click on a word span ---
                "   function onWordClick(event) { " +
                "       event.stopPropagation(); " + // Prevent click bubbling up further
                "       var word = event.target.innerText.trim(); " +
                "       console.log('Word clicked: \"' + word + '\"'); " +
                "       if (word && word.length > 0 && word.length < 50) { " + // Basic validation
                "           try { " +
                "               AndroidInterface.processTextSelection(word); " +
                "               console.log('Called AndroidInterface for word: ' + word); " +
                "           } catch (err) { " +
                "               console.error('Error calling AndroidInterface: ' + err); " +
                "           } " +
                "       } else { "+
                "            console.log('Clicked word invalid or too long.'); "+
                "       } "+
                "   } " +
                // --- Recursive function to process nodes ---
                // targetTags: Only wrap words inside these tag types (e.g., 'P', 'DIV', 'SPAN', 'LI', 'TD')
                // ignoreTags: Do not descend into these tags (e.g., 'SCRIPT', 'STYLE', 'A', 'BUTTON')
                "   function wrapWords(node, targetTags, ignoreTags) { " +
                "       if (!node) return; " +
                "       const nodeName = node.nodeName.toUpperCase(); "+
                // Skip nodes we should ignore or their children
                "       if (ignoreTags.includes(nodeName)) { return; } "+

                // Process child nodes first (depth-first)
                "       let child = node.firstChild; "+
                "       while(child){ "+
                // Store next sibling *before* potentially modifying/removing child
                "           let nextChild = child.nextSibling; "+
                "           wrapWords(child, targetTags, ignoreTags); "+
                "           child = nextChild; "+
                "       } "+

                // Now process the current node if it's a text node inside a target tag
                "       if (node.nodeType === 3 && node.parentNode && targetTags.includes(node.parentNode.nodeName.toUpperCase())) { " + // Node.TEXT_NODE is 3
                "           let textContent = node.nodeValue; " +
                // Regex to find words (sequences of non-whitespace chars) and whitespace
                "           let parts = textContent.split(/(\\s+)/).filter(part => part.length > 0); "+
                "           if (parts.length > 1 || (parts.length === 1 && !/^\\s+$/.test(parts[0]))) { "+ // Only process if there's actual content / multiple parts
                "               let parent = node.parentNode; " +
                "               console.log('Wrapping text in:', parent.nodeName, ':', textContent.substring(0,30)+'...'); "+
                "               for (let i = 0; i < parts.length; i++) { " +
                "                   let part = parts[i]; " +
                // If it's not just whitespace, wrap it in a span
                "                   if (!/^\\s+$/.test(part)) { " +
                "                       let span = document.createElement('span'); " +
                "                       span.textContent = part; " +
                // Add a class for potential styling/identification (optional)
                "                       span.classList.add('clickable-word'); "+
                "                       span.addEventListener('click', onWordClick); " +
                "                       parent.insertBefore(span, node); " +
                "                   } else { " +
                // If it's whitespace, insert it as a text node
                "                       parent.insertBefore(document.createTextNode(part), node); " +
                "                   } " +
                "               } " +
                // Remove the original undivided text node
                "               parent.removeChild(node); " +
                "           } "+
                "       } " +
                "   } " +

                "   const TARGET_CONTAINER_TAGS = ['P', 'DIV', 'SPAN', 'LI', 'TD', 'H1', 'H2', 'H3', 'H4', 'H5', 'H6', 'ARTICLE', 'SECTION', 'BLOCKQUOTE']; "+
                "   const IGNORE_CHILDREN_TAGS = ['SCRIPT', 'STYLE', 'A', 'BUTTON', 'INPUT', 'TEXTAREA', 'SELECT', 'IMG', 'VIDEO', 'AUDIO', 'CANVAS', 'SVG', 'CODE', 'PRE', 'NOSCRIPT']; "+

                "   try { "+
                "       console.log('Calling wrapWords on document.body');"+
                "       wrapWords(document.body, TARGET_CONTAINER_TAGS, IGNORE_CHILDREN_TAGS); " +
                "       console.log('Word wrapping finished.'); " +
                "   } catch (err) { "+
                "        console.error('Error during word wrapping:', err); "+
                "   } "+
                "})();"; // Immediately invoke the function

        // Ensure webView is not null before attempting to load URL
        if (targetWebView != null) {
            targetWebView.loadUrl(js);
            Log.i(BROWSER_TAG, "Injected Word Wrapping JS.");
        } else {
            Log.e(BROWSER_TAG, "Attempted to inject Word Wrapping JS but WebView was null!");
        }
    }

    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void processTextSelection(String text) { // Method name is fine, even though it's now from a click
            Log.i(JS_INTERFACE_TAG, ">>>> processTextSelection (from word click) called with text: \"" + text + "\" <<<<");

            if (getActivity() == null) {
                Log.e(JS_INTERFACE_TAG, "Activity is null, cannot show BottomSheet!");
                return;
            }

            getActivity().runOnUiThread(() -> {
                Log.d(JS_INTERFACE_TAG, "Running on UI thread to show BottomSheet for: " + text);
                try {
                    FragmentManager fm = getParentFragmentManager();
                    if (!fm.isStateSaved() && !fm.isDestroyed()) {
                        DictionaryBottomSheetDialog bottomSheet = DictionaryBottomSheetDialog.newInstance(text);
                        bottomSheet.show(fm, DictionaryBottomSheetDialog.TAG);
                        Log.i(JS_INTERFACE_TAG, "BottomSheet shown successfully.");
                    } else {
                        Log.e(JS_INTERFACE_TAG, "Cannot show BottomSheet: FragmentManager state saved or destroyed.");
                    }
                } catch (Exception e) {
                    Log.e(JS_INTERFACE_TAG, "Error showing BottomSheetDialog", e);
                }
            });
        }
    }

    // Methods for MainActivity to control WebView navigation (Remain the same)
    public boolean canGoBackInWebView() {
        return webView != null && webView.canGoBack();
    }

    public void goBackInWebView() {
        if (webView != null) {
            webView.goBack();
        }
    }

    // Lifecycle Cleanup (Remains mostly the same)
    @Override
    public void onDestroyView() {
        Log.d(BROWSER_TAG, "onDestroyView called.");
        if (webView != null) {
            Log.d(BROWSER_TAG, "Cleaning up WebView.");
            if (webView.getParent() instanceof ViewGroup) {
                ((ViewGroup) webView.getParent()).removeView(webView);
                Log.d(BROWSER_TAG, "WebView removed from parent ViewGroup.");
            } else {
                Log.w(BROWSER_TAG, "WebView parent is not a ViewGroup or is null.");
            }
            webView.removeJavascriptInterface("AndroidInterface"); // Still remove interface
            Log.d(BROWSER_TAG, "JavaScript interface removed.");
            webView.clearCache(true);
            webView.clearHistory();
            webView.setWebViewClient(null);
            webView.setWebChromeClient(null);
            webView.destroy();
            webView = null;
            Log.d(BROWSER_TAG, "WebView destroyed.");
        }
        super.onDestroyView();
    }
}