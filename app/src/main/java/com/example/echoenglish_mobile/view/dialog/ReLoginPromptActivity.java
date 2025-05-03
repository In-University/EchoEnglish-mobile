package com.example.echoenglish_mobile.view.dialog;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.view.activity.auth.LoginActivity;

public class ReLoginPromptActivity extends AppCompatActivity {

    private Dialog loginDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        showLoginRequiredDialog();
    }

    private void showLoginRequiredDialog() {
        loginDialog = new Dialog(this);
        loginDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loginDialog.setContentView(R.layout.dialog_relogin);

        Window window = loginDialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvTitle = loginDialog.findViewById(R.id.tvTitle);
        TextView tvMessage = loginDialog.findViewById(R.id.tvMessage);
        Button btnCancel = loginDialog.findViewById(R.id.btnCancel);
        Button btnLogin = loginDialog.findViewById(R.id.btnLogin);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        if (title != null && !title.isEmpty()) {
            tvTitle.setText(title);
        }

        if (message != null && !message.isEmpty()) {
            tvMessage.setText(message);
        }

        btnCancel.setOnClickListener(v -> {
            loginDialog.dismiss();
            finish();
        });

        btnLogin.setOnClickListener(v -> {
            Intent loginIntent = new Intent(ReLoginPromptActivity.this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginIntent);
            loginDialog.dismiss();
            finish();
        });

        loginDialog.setCancelable(false);
        loginDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loginDialog != null && loginDialog.isShowing()) {
            loginDialog.dismiss();
        }
    }
}