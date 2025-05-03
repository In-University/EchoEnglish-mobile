package com.example.echoenglish_mobile.network;

import android.content.Context;
import android.content.Intent;

import com.example.echoenglish_mobile.util.MyApp;
import com.example.echoenglish_mobile.view.dialog.ReLoginPromptActivity;


public class ForbiddenHandler {
    public static void handleForbidden() {
        Context context = MyApp.getAppContext();
        Intent intent = new Intent(context, ReLoginPromptActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
