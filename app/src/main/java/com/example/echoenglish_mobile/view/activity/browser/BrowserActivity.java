package com.example.echoenglish_mobile.view.activity.browser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.adapter.ShortcutAdapter;
import com.example.echoenglish_mobile.view.fragment.BrowserFragment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class BrowserActivity extends AppCompatActivity implements ShortcutAdapter.OnShortcutClickListener{

    private static final String TAG = "BrowserActivity";
    private static final String BROWSER_FRAGMENT_TAG = "BrowserFragmentInstance";
    public static final String EXTRA_URL = "EXTRA_URL";
    ImageView btnClose;
    private EditText urlEditText;
    private RecyclerView shortcutRecyclerView;
    private ShortcutAdapter shortcutAdapter;
    private List<ShortcutAdapter.Shortcut> shortcutList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        urlEditText = findViewById(R.id.urlEditText);
        btnClose = findViewById(R.id.btnClose);
        shortcutRecyclerView = findViewById(R.id.shortcutRecyclerView);
        String initialUrl = getIntent().getStringExtra(EXTRA_URL);
        if (initialUrl != null && !initialUrl.isEmpty()) {
            urlEditText.setText(initialUrl);
            loadUrlInFragment(initialUrl);
        }
        btnClose.setOnClickListener(v -> finish());
        setupUrlBarListener();
        setupRecyclerView();
        loadInitialShortcuts();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setupUrlBarListener() {
        urlEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String input = urlEditText.getText().toString().trim();
                if (!input.isEmpty()) {
                    hideKeyboard(v);
                    if (isUrl(input)) {
                        loadUrlInFragment(input);
                    } else {
                        searchOnGoogle(input);
                    }
                }
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        shortcutList = new ArrayList<>();
        shortcutAdapter = new ShortcutAdapter(shortcutList, this);
        shortcutRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        shortcutRecyclerView.setAdapter(shortcutAdapter);
    }

    private void loadInitialShortcuts() {
        shortcutList.clear();
        shortcutList.add(new ShortcutAdapter.Shortcut("CNN", "https://cnn.com", R.drawable.logo_cnn));
        shortcutList.add(new ShortcutAdapter.Shortcut("Reuters", "https://reuters.com", R.drawable.logo_reuter));
        shortcutList.add(new ShortcutAdapter.Shortcut("Guardian", "https://theguardian.com", R.drawable.logo_guardian));
        shortcutList.add(new ShortcutAdapter.Shortcut("The New York Times", "https://nytimes.com", R.drawable.logo_newyorktimes));
        shortcutAdapter.notifyDataSetChanged();
    }

    @Override
    public void onShortcutClick(ShortcutAdapter.Shortcut shortcut) {
        if (shortcut.getUrl() != null && !shortcut.getUrl().isEmpty()) {
            loadUrlInFragment(shortcut.getUrl());
            urlEditText.setText(shortcut.getUrl());
        } else {
            Toast.makeText(this, "Clicked: " + shortcut.getName(), Toast.LENGTH_SHORT).show();
        }
    }
    private void loadUrlInFragment(String url) {
        String finalUrl = url;
        if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
            finalUrl = "https://" + finalUrl;
        }
        Log.i(TAG, "Loading URL in Fragment: " + finalUrl);
        showBrowserFragment(finalUrl);
    }

    private void searchOnGoogle(String query) {
        Log.i(TAG, "Searching Google for: " + query);
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String searchUrl = "https://www.google.com/search?q=" + encodedQuery;
            showBrowserFragment(searchUrl);
            urlEditText.setText(query);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Error encoding search query", e);
            Toast.makeText(this, "Lỗi tạo link tìm kiếm", Toast.LENGTH_SHORT).show();
        }
    }

    private void showBrowserFragment(String url) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment existingFragment = fm.findFragmentByTag(BROWSER_FRAGMENT_TAG);

        if (existingFragment instanceof BrowserFragment) {
            Log.d(TAG, "Replacing existing BrowserFragment");
        } else {
            Log.d(TAG, "Creating new BrowserFragment");
        }

        BrowserFragment browserFragment = BrowserFragment.newInstance(url);
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_container, browserFragment, BROWSER_FRAGMENT_TAG);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        BrowserFragment currentFragment = (BrowserFragment) fm.findFragmentByTag(BROWSER_FRAGMENT_TAG);

        // 1. Check if the fragment exists and can handle the back press (go back in web history)
        if (currentFragment != null && currentFragment.isVisible() && currentFragment.canGoBackInWebView()) {
            Log.d(TAG, "Back pressed: Telling BrowserFragment to go back.");
            currentFragment.goBackInWebView();
        }
        // 2. If fragment can't go back, or doesn't exist, check fragment back stack
        else if (fm.getBackStackEntryCount() > 0) {
            Log.d(TAG, "Back pressed: Popping BrowserFragment from back stack.");
            fm.popBackStack(); // This will trigger onBackStackChanged
        }
        // 3. If fragment back stack is empty, let the activity handle it (usually closes)
        else {
            Log.d(TAG, "Back pressed: No fragment history, closing activity.");
            super.onBackPressed();
        }
    }

    private boolean isUrl(String text) {
        return Patterns.WEB_URL.matcher(text).matches() || text.contains(".");
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}