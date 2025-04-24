package com.example.echoenglish_mobile.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PurchaseManager {

    private static final String PREFS_NAME = "PurchasePrefs";
    private static final String PURCHASED_PREFIX = "purchased_";

    private SharedPreferences sharedPreferences;

    public PurchaseManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isPurchased(Long flashcardId) {
        if (flashcardId == null) return false;
        return sharedPreferences.getBoolean(PURCHASED_PREFIX + flashcardId, false);
    }

    public void setPurchased(Long flashcardId, boolean purchased) {
        if (flashcardId == null) return;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PURCHASED_PREFIX + flashcardId, purchased);
        editor.apply(); // Lưu bất đồng bộ
    }
}